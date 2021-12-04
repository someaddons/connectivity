package com.connectivity.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin
{
    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    public void on(final ChannelHandlerContext packet, final Throwable component, final CallbackInfo ci)
    {
        ci.cancel();
    }
}
