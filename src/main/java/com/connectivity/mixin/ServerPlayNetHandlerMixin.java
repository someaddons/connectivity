package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.network.play.ServerPlayNetHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = ServerPlayNetHandler.class, priority = 101)
/**
 * Sets the timeout constant
 */
public class ServerPlayNetHandlerMixin
{
    @ModifyConstant(method = "tick", constant = @Constant(longValue = 15000L, ordinal = 0), require = 0, expect = 0)
    public long playTimeout(long old)
    {
        return Connectivity.config.getCommonConfig().disconnectTimeout.get() * 1000;
    }
}
