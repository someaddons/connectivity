package com.connectivity.mixin;

import com.connectivity.Connectivity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;

@Mixin(Connection.class)
public abstract class NetworkManagerMixin
{
    @Shadow
    private Channel channel;

    @Shadow
    public abstract void disconnect(final Component p_129508_);

    @Unique
    private int counter = 0;

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void on(final ChannelHandlerContext packet, final Throwable component, final CallbackInfo ci)
    {
        if (Connectivity.config.getCommonConfig().debugPrintMessages && !(component instanceof ClosedChannelException))
        {
            Connectivity.LOGGER.warn("Network error for:" + packet.name(), component);
        }

        if (!channel.isOpen())
        {
            counter++;
            if (counter >= 10)
            {
                if (counter == 10 && Connectivity.server != null && Connectivity.server.getConnection() != null)
                {
                    disconnect(Component.literal("Forced closure due to network errors"));
                    Connectivity.server.submit(() -> Connectivity.server.getConnection().getConnections().remove((Connection) (Object) this));
                    packet.close();
                }
            }
        }
    }
}
