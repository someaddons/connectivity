package com.connectivity.networkstats;

import com.connectivity.Connectivity;
import com.google.common.util.concurrent.AtomicDouble;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;

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
    private static final Set<String>                    BLOCKED                = ConcurrentHashMap.newKeySet();
    private static final Set<String>                    LOGGED_IN_IPS          = ConcurrentHashMap.newKeySet();
    private static final ConcurrentMap<String, Integer> errorCounts            = new ConcurrentHashMap<>();
    private static final AtomicInteger                  ERROR_COUNT            = new AtomicInteger();
    private static final AtomicLong                     LAST_ERROR_RATE_UPDATE = new AtomicLong();
    private static final AtomicDouble                   ERROR_RATE_PER_SEC     = new AtomicDouble();
    public static final  AtomicInteger                  ACTIVE_CHANNELS        = new AtomicInteger();
    public static final  AttributeKey<String>           FROZEN                 = AttributeKey.valueOf("frozen");

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
        if (LOGGED_IN_IPS.contains(identifier) || BLOCKED.contains(identifier))
        {
            return;
        }

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
                ERROR_RATE_PER_SEC.set((ERROR_RATE_PER_SEC.get() * 9 + rate) / 10);
            }
        }

        int count = errorCounts.merge(identifier, 1, Integer::sum);
        if (count >= calculateThreshold())
        {
            BLOCKED.add(identifier);
            errorCounts.remove(identifier);
            Connectivity.LOGGER.warn("Ignoring further traffic from: " + identifier + " for repeated malformed traffic.");
            closeChannel(ctx);
        }
    }

    private static double calculateThreshold()
    {
        double rate = ERROR_RATE_PER_SEC.get();
        int threshold = (int) Math.ceil(25 / (1 + rate / 2.0));
        return Math.max(3, Math.min(threshold, 25));
    }

    public static void onLogin(final Channel channel)
    {
        LOGGED_IN_IPS.add(getIdentifier(channel));
        errorCounts.remove(getIdentifier(channel));
    }

    private static void closeChannel(ChannelHandlerContext ctx)
    {
        ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);
        ctx.pipeline().remove(ReadTimeoutHandler.class);

        String identifier = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        if (Connectivity.config.getCommonConfig().proxyWhitelist.contains(identifier))
        {
            // Freeze the channel instead of closing it
            freezeChannel(ctx, 60);
        }
        else
        {
            ctx.close();
        }
    }

    public static void freezeChannel(final ChannelHandlerContext ctx, final int secondsTimeout)
    {
        if (!ctx.channel().hasAttr(FROZEN))
        {
            ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);
            ctx.channel().config().setOption(ChannelOption.SO_RCVBUF, 16 * 1024);
            ctx.channel().config().setOption(ChannelOption.SO_SNDBUF, 16 * 1024);

            ctx.channel().attr(FROZEN).set("true");
            ctx.executor().schedule(() -> {
                if (ctx.channel().isOpen())
                {
                    ctx.close();
                }
            }, secondsTimeout, TimeUnit.SECONDS);
        }
    }

    private static String getIdentifier(final Channel channel)
    {
        final SocketAddress adr = channel.remoteAddress();

        if (!(adr instanceof InetSocketAddress inetSocketAddress))
        {
            return "local:" + adr;
        }

        String identifier = inetSocketAddress.getAddress().getHostAddress();
        if (Connectivity.config.getCommonConfig().proxyWhitelist.contains(identifier))
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
}
