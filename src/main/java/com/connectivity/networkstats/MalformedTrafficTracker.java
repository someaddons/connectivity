package com.connectivity.networkstats;

import com.connectivity.Connectivity;
import com.google.common.util.concurrent.AtomicDouble;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracker for network errors by IP
 */
public class MalformedTrafficTracker
{
    private static final int                                 BLOCK_THRESHOLD        = 40;
    private static final long                                ERROR_WINDOW_NANOS     = TimeUnit.SECONDS.toNanos(60);
    private static final long                                ERROR_SAMETIME_NANOS   = TimeUnit.MILLISECONDS.toNanos(500);
    private static final Set<String>                         BLOCKED                = ConcurrentHashMap.newKeySet();
    private static final ConcurrentMap<String, ErrorTracker> errorCounts            = new ConcurrentHashMap<>();
    private static final AtomicInteger                       ERROR_COUNT            = new AtomicInteger();
    private static final AtomicLong                          LAST_ERROR_RATE_UPDATE = new AtomicLong();
    private static final AtomicDouble                        ERROR_RATE_PER_SEC     = new AtomicDouble();
    public static final  AtomicInteger                       ACTIVE_CHANNELS        = new AtomicInteger();
    public static final  AttributeKey<String>                FROZEN                 = AttributeKey.valueOf("connectivity_frozen");
    private static final AttributeKey<Long>                  LAST_ERROR             = AttributeKey.valueOf("connectivity_last_error");
    private static final AttributeKey<Integer>               LAST_ERROR_COUNTS      = AttributeKey.valueOf("connectivity_last_error_counts");
    private static final AttributeKey<Boolean>               LOGIN                  = AttributeKey.valueOf("connectivity_logged_in");

    public static boolean isBlocked(ChannelHandlerContext ctx)
    {
        if (!Connectivity.config.getCommonConfig().enableMalformedTrafficDetection)
        {
            return false;
        }

        return BLOCKED.contains(getIdentifier(ctx.channel()));
    }

    public static void removedBlocked(final String identifier)
    {
        BLOCKED.remove(identifier);
    }

    public static void recordError(final ChannelHandlerContext ctx)
    {
        final String identifier = getIdentifier(ctx.channel());
        if ((ctx.channel().hasAttr(LOGIN) && ctx.channel().attr(LOGIN).get()) || BLOCKED.contains(identifier) || ctx.channel().hasAttr(FROZEN))
        {
            return;
        }

        final long currentNano = System.nanoTime();
        final Long lastError = ctx.channel().attr(LAST_ERROR).get();
        if (lastError != null && currentNano - lastError < ERROR_SAMETIME_NANOS)
        {
            if (!ctx.channel().hasAttr(LAST_ERROR_COUNTS))
            {
                ctx.channel().attr(LAST_ERROR_COUNTS).set(1);
                return;
            }

            final int nextErrorCount = ctx.channel().attr(LAST_ERROR_COUNTS).get() + 1;
            ctx.channel().attr(LAST_ERROR_COUNTS).set(nextErrorCount);
            if (nextErrorCount < 15)
            {
                return;
            }
        }

        ctx.channel().attr(LAST_ERROR).set(currentNano);
        ctx.channel().attr(LAST_ERROR_COUNTS).set(1);

        ERROR_COUNT.incrementAndGet();
        final long now = System.currentTimeMillis();
        final long last = LAST_ERROR_RATE_UPDATE.get();
        final long elapsed = now - last;

        if (elapsed > 1000)
        {
            if (LAST_ERROR_RATE_UPDATE.compareAndSet(last, now))
            {
                int count = ERROR_COUNT.getAndSet(0);
                double rate = count * 1000.0 / elapsed; // errors per second

                if (elapsed > 1000 * 100)
                {
                    ERROR_RATE_PER_SEC.set(rate);
                }
                else
                {
                    ERROR_RATE_PER_SEC.set((ERROR_RATE_PER_SEC.get() * 9 + rate) / 10);
                }
            }
        }

        int count = recordErrorFor(identifier, currentNano);
        if (count >= BLOCK_THRESHOLD)
        {
            BLOCKED.add(identifier);
            errorCounts.remove(identifier);
            Connectivity.LOGGER.warn("Ignoring further traffic from: " + identifier + " for repeated malformed traffic.");
            closeChannel(ctx);
        }
        else if (count >= calculateFreezeThreshold())
        {
            freezeChannel(ctx, freezeTimeoutSeconds());
        }
    }

    private static int recordErrorFor(final String identifier, final long now)
    {
        ErrorTracker tracker = errorCounts.compute(identifier, (key, current) -> {
            if (current == null || now - current.lastErrorTime >= ERROR_WINDOW_NANOS)
            {
                return new ErrorTracker(now);
            }

            current.count++;
            current.lastErrorTime = now;
            return current;
        });

        return tracker.count;
    }

    private static int calculateFreezeThreshold()
    {
        double rate = ERROR_RATE_PER_SEC.get();
        int threshold = (int) Math.ceil(15 / (1 + rate / 2.0));
        return Math.max(3, Math.min(threshold, 15));
    }

    public static void onLogin(final Channel channel)
    {
        channel.attr(LOGIN).set(true);
    }

    private static void closeChannel(ChannelHandlerContext ctx)
    {
        ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);
        if (ctx.pipeline().context(ReadTimeoutHandler.class) != null)
        {
            ctx.pipeline().remove(ReadTimeoutHandler.class);
        }

        final InetAddress adress = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();
        if (isProxy(adress))
        {
            // Freeze the channel instead of closing it
            freezeChannel(ctx, 60);
        }
        else
        {
            ctx.close();
        }
    }

    /**
     * Checks if the adress is a proxy
     *
     * @param address
     * @return true if configured proxy or loopback
     */
    private static boolean isProxy(final InetAddress address)
    {
        return address.isLoopbackAddress() || Connectivity.config.getCommonConfig().proxyWhitelist.contains(address.getHostAddress());
    }

    public static void freezeChannel(final ChannelHandlerContext ctx, final int secondsTimeout)
    {
        if (ctx.channel().hasAttr(FROZEN))
        {
            return;
        }

        if (Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            Connectivity.LOGGER.warn("Temporarily suspending traffic from {} for {} seconds due to suspicious network activity.", getIdentifier(ctx.channel()), secondsTimeout);
        }
        ctx.channel().attr(FROZEN).set("true");

        ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);
        ctx.channel().config().setOption(ChannelOption.SO_RCVBUF, 16 * 1024);
        ctx.channel().config().setOption(ChannelOption.SO_SNDBUF, 16 * 1024);

        if (ctx.pipeline().context(ReadTimeoutHandler.class) != null)
        {
            ctx.pipeline().remove(ReadTimeoutHandler.class);
        }

        ctx.executor().schedule(() -> {
            if (ctx.channel().isOpen())
            {
                ctx.close();
            }
        }, secondsTimeout, TimeUnit.SECONDS);
    }

    private static String getIdentifier(final Channel channel)
    {
        final SocketAddress adr = channel.remoteAddress();

        if (!(adr instanceof InetSocketAddress inetSocketAddress))
        {
            return "local:" + adr;
        }

        String identifier = inetSocketAddress.getAddress().getHostAddress();
        if (isProxy(inetSocketAddress.getAddress()))
        {
            identifier = channel.toString();
        }
        return identifier;
    }

    public static int freezeTimeoutSeconds()
    {
        int channels = ACTIVE_CHANNELS.get();

        if (channels < 2000)
        {
            return 30;
        }
        if (channels < 3000)
        {
            return 15;
        }
        if (channels < 5000)
        {
            return 7;
        }
        return 3;
    }

    private static class ErrorTracker
    {
        int  count;
        long lastErrorTime;

        ErrorTracker(long now)
        {
            this.count = 1;
            this.lastErrorTime = now;
        }
    }
}
