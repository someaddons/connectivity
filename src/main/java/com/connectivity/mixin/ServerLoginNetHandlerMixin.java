package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.network.login.ServerLoginNetHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = ServerLoginNetHandler.class, priority = 1001)
/**
 * Set the login tick timeout
 */
public class ServerLoginNetHandlerMixin
{
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 600, ordinal = 0), require = 0, expect = 0)
    public int loginTimeout(int old)
    {
        return Connectivity.config.getCommonConfig().logintimeout.get();
    }
}
