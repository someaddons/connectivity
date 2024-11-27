package com.connectivity.mixin;

import com.connectivity.Connectivity;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/network/Connection$1")
public class ConnectionTimeoutMixin
{
    @Redirect(method = "initChannel", at = @At(value = "NEW", target = "io/netty/handler/timeout/ReadTimeoutHandler", remap = false), require = 0, expect = 0)
    private ReadTimeoutHandler aVoid(final int timeoutSeconds)
    {
        return new ReadTimeoutHandler((int) (Connectivity.config.getCommonConfig().disconnectTimeout));
    }
}
