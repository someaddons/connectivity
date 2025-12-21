package com.connectivity.mixin.malformedtraffic;

import com.connectivity.networkstats.MalformedTrafficTracker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.channels.ClosedChannelException;

import static com.connectivity.networkstats.MalformedTrafficTracker.ACTIVE_CHANNELS;
import static com.connectivity.networkstats.MalformedTrafficTracker.freezeTimeoutSeconds;

@Mixin(targets = "net/minecraft/server/network/ServerConnectionListener$1")
/**
 * Custom channel handler for traffic blocking
 */
public class ChannelHandler
{
    @Inject(method = "initChannel", at = @At("RETURN"))
    private void injectEarlyHandler(final Channel channel, final CallbackInfo ci)
    {
        final ChannelPipeline pipeline = channel.pipeline();
        pipeline.addFirst("malformed_ip_blocker", new ChannelInboundHandlerAdapter()
        {
            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception
            {
                ACTIVE_CHANNELS.decrementAndGet();

                super.channelInactive(ctx);
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception
            {
                ACTIVE_CHANNELS.incrementAndGet();

                // Removes the channel identity string from the blocked list, NOT its IP
                MalformedTrafficTracker.removedBlocked(ctx.channel().toString());

                if (MalformedTrafficTracker.isBlocked(ctx))
                {
                    MalformedTrafficTracker.freezeChannel(ctx, freezeTimeoutSeconds());
                    return;
                }

                super.channelActive(ctx);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
            {
                if (MalformedTrafficTracker.isBlocked(ctx))
                {
                    return;
                }

                try
                {
                    super.channelRead(ctx, msg);
                }
                catch (Exception e)
                {
                    MalformedTrafficTracker.recordError(ctx);
                    if (!MalformedTrafficTracker.isBlocked(ctx))
                    {
                        throw e;
                    }
                }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            {
                if (MalformedTrafficTracker.isBlocked(ctx))
                {
                    return;
                }
                if (!(cause instanceof ClosedChannelException))
                {
                    MalformedTrafficTracker.recordError(ctx);
                }

                ctx.fireExceptionCaught(cause);
            }
        });
    }
}
