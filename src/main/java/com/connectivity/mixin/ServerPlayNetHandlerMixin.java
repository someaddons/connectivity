package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 1001)
/**
 * Sets the timeout constant
 */
public class ServerPlayNetHandlerMixin
{
    @ModifyConstant(method = "tick", constant = @Constant(longValue = 15000L, ordinal = 0), require = 0, expect = 0)
    public long playTimeout(long old)
    {
        return Connectivity.config.getCommonConfig().disconnectTimeout * 1000;
    }
}
