package com.connectivity.mixin;

import com.connectivity.Connectivity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.channels.ClosedChannelException;

@Mixin(Connection.class)
public abstract class ConnectionMixin
{
    @Shadow
    public abstract void disconnect(final Component p_129508_);

    @Shadow
    private volatile PacketListener packetListener;
    @Unique
    private          int            counter = 0;

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void on(final ChannelHandlerContext context, final Throwable throwable, final CallbackInfo ci)
    {
        if (throwable instanceof ClosedChannelException)
        {
            counter++;
            if (counter >= 10)
            {
                if (counter == 10)
                {
                    if (packetListener instanceof ServerGamePacketListenerImpl serverGamePacketListener)
                    {
                        disconnect(Component.literal("Forced closure due to network errors: " + throwable));
                        synchronized (serverGamePacketListener.getPlayer().level().getServer().getConnection().getConnections())
                        {
                            serverGamePacketListener.getPlayer().level().getServer().getConnection().getConnections().remove((Connection) (Object) this);
                            context.close();
                        }
                    }
                }
            }
            return;
        }

        if (Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            if (packetListener instanceof ServerGamePacketListenerImpl serverGamePacketListener)
            {
                Connectivity.LOGGER.warn("Network error in:" + context.name() + " for player:" + serverGamePacketListener.getPlayer().getDisplayName(), throwable);
            }
            else
            {
                Connectivity.LOGGER.warn("Network error in:" + context.name() + " with:" + packetListener, throwable);
            }
        }
    }
}
