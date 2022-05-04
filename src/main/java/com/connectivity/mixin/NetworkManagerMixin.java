package com.connectivity.mixin;

import com.connectivity.Connectivity;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class NetworkManagerMixin
{
    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void on(final ChannelHandlerContext packet, final Throwable component, final CallbackInfo ci)
    {
        if (Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            Connectivity.LOGGER.warn("Network error for:" + packet.name(), component);
        }
    }
}
