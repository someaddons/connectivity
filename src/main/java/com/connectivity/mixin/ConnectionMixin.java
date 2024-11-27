package com.connectivity.mixin;

import com.connectivity.Connectivity;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.TimeUnit;

@Mixin(Connection.class)
public abstract class ConnectionMixin
{
    @Shadow
    @Nullable
    private volatile PacketListener packetListener;

    @Shadow
    public abstract void disconnect(final Component p_129508_);

    @Unique
    private int counter = 0;

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void on(final ChannelHandlerContext context, final Throwable throwable, final CallbackInfo ci)
    {
        counter++;
        if (Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            if (!(throwable instanceof ClosedChannelException))
            {
                if (packetListener instanceof ServerGamePacketListenerImpl serverGamePacketListener)
                {
                    Connectivity.LOGGER.warn("Network error in:" + context.name() + " for player:" + serverGamePacketListener.getPlayer().getName().getString(), throwable);
                }
                else
                {
                    Connectivity.LOGGER.warn("Network error in:" + context.name() + " with:" + packetListener, throwable);
                }
            }
        }

        // Error handling for many exceptions but traditional disconnect attempts failed
        if (counter >= 20)
        {
            counter = -100000;
            this.disconnect(Component.literal("Too many network errors"));
            context.channel().disconnect().addListener(future -> {
                if (!future.isSuccess())
                {
                    if (Connectivity.config.getCommonConfig().debugPrintMessages)
                    {
                        Connectivity.LOGGER.warn("Failed to disconnect channel: " + context.channel().remoteAddress());
                    }
                }

                context.channel().deregister().addListener(dereFuture -> {
                    if (dereFuture.isSuccess())
                    {
                        if (Connectivity.config.getCommonConfig().debugPrintMessages)
                        {
                            Connectivity.LOGGER.warn("Channel deregistered: " + context.channel().remoteAddress());
                        }
                    }
                    else
                    {
                        if (Connectivity.config.getCommonConfig().debugPrintMessages)
                        {
                            Connectivity.LOGGER.warn("Failed to deregister channel: " + context.channel().remoteAddress());
                        }
                    }

                    context.channel().close().addListener(closeFuture -> {
                        if (closeFuture.isSuccess())
                        {
                            if (Connectivity.config.getCommonConfig().debugPrintMessages)
                            {
                                Connectivity.LOGGER.warn("Channel closed: " + context.channel().remoteAddress());
                            }
                        }
                        else
                        {
                            if (Connectivity.config.getCommonConfig().debugPrintMessages)
                            {
                                Connectivity.LOGGER.warn("Failed to close channel: " + context.channel().remoteAddress());
                            }
                        }

                        if (Connectivity.config.getCommonConfig().debugPrintMessages)
                        {
                            Connectivity.LOGGER.warn("Removing Handlers");
                        }
                        for (String name : context.channel().pipeline().names())
                        {
                            context.channel().pipeline().remove(name);
                        }
                    });
                });
            });
        }
    }

    @Redirect(method = "disconnect", at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelFuture;awaitUninterruptibly()Lio/netty/channel/ChannelFuture;"))
    private ChannelFuture onWait(final ChannelFuture instance)
    {
        try
        {
            instance.await(10, TimeUnit.SECONDS);
            return null;
        }
        catch (InterruptedException e)
        {
            Connectivity.LOGGER.warn("Interrupted thread:" + Thread.currentThread());
        }

        return null;
    }
}
