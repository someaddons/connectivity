package com.connectivity.mixin;

import com.connectivity.Connectivity;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.channels.ClosedChannelException;

@Mixin(Connection.class)
public class ConnectionErrorMixin
{
    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void on(final ChannelHandlerContext channelHandlerContext, final Throwable throwable, final CallbackInfo ci)
    {
        if (throwable.getStackTrace() != null && Connectivity.config.getCommonConfig().debugPrintMessages && !(throwable instanceof ClosedChannelException))
        {
            Connectivity.LOGGER.warn(throwable);
        }
    }
}
