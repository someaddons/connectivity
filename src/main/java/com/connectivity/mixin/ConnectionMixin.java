package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.TimeUnit;

@Mixin(Connection.class)
public abstract class ConnectionMixin
{
    @Shadow
    private volatile PacketListener packetListener;

    @Shadow
    public abstract void disconnect(final Component p_129508_);

    @Shadow
    private Component disconnectedReason;
    @Unique
    private int       counter = 0;

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void on(final ChannelHandlerContext context, final Throwable throwable, final CallbackInfo ci)
    {
        counter++;
        if (Connectivity.config.getCommonConfig().debugPrintMessages && disconnectedReason == null)
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

                if (lastPacket != null)
                {
                    PacketLogging.logPacket(lastPacket, "Printing packet to send:");
                    lastPacket = null;
                }
            }
        }

        // Error handling for many exceptions but traditional disconnect attempts failed
        if (counter >= 20)
        {
            String temp = "unknown";
            if (packetListener instanceof ServerGamePacketListenerImpl serverGamePacketListener)
            {
                temp = serverGamePacketListener.getPlayer().getName().getString();
            }

            final String name = temp;

            counter = -100000;
            this.disconnect(Component.literal("Too many network errors"));
            context.channel().disconnect().addListener(future -> {
                if (!future.isSuccess())
                {
                    if (Connectivity.config.getCommonConfig().debugPrintMessages)
                    {
                        Connectivity.LOGGER.warn("Failed to disconnect channel for: " + name);
                    }
                }

                context.channel().deregister().addListener(dereFuture -> {
                    if (dereFuture.isSuccess())
                    {
                        if (Connectivity.config.getCommonConfig().debugPrintMessages)
                        {
                            Connectivity.LOGGER.warn("Channel deregistered for: " + name);
                        }
                    }
                    else
                    {
                        if (Connectivity.config.getCommonConfig().debugPrintMessages)
                        {
                            Connectivity.LOGGER.warn("Failed to deregister channel for: " + name);
                        }
                    }

                    context.channel().close().addListener(closeFuture -> {
                        if (closeFuture.isSuccess())
                        {
                            if (Connectivity.config.getCommonConfig().debugPrintMessages)
                            {
                                Connectivity.LOGGER.warn("Channel closed for: " + name);
                            }
                        }
                        else
                        {
                            if (Connectivity.config.getCommonConfig().debugPrintMessages)
                            {
                                Connectivity.LOGGER.warn("Failed to close channel for: " + name);
                            }
                        }

                        for (String handler : context.channel().pipeline().names())
                        {
                            try
                            {
                                context.channel().pipeline().remove(handler);
                            }
                            catch (Throwable e)
                            {
                                // noop
                            }
                        }
                    });
                });
            });
        }
    }

    @Redirect(method = "disconnect", at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelFuture;awaitUninterruptibly()Lio/netty/channel/ChannelFuture;", remap = false), require = 0)
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

    @Unique
    private Packet<?> lastPacket = null;

    @Inject(method = "doSendPacket", at = @At("HEAD"))
    private void onSend(
        final Packet<?> p_243260_,
        final PacketSendListener p_243290_,
        final ConnectionProtocol p_243203_,
        final ConnectionProtocol p_243307_,
        final CallbackInfo ci)
    {
        lastPacket = p_243260_;
    }

    @Inject(method = "doSendPacket", at = @At("RETURN"))
    private void afterSend(
        final Packet<?> p_243260_,
        final PacketSendListener p_243290_,
        final ConnectionProtocol p_243203_,
        final ConnectionProtocol p_243307_,
        final CallbackInfo ci)
    {
        lastPacket = null;
    }
}
