package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = ClientboundCustomPayloadPacket.class, priority = 1)
public class ClientboundCustomPayloadPacketMixin
{
    @ModifyConstant(method = "method_56460", constant = @Constant(intValue = 1048576), require = 0)
    private static int modifyLimit(final int constant)
    {
        if (!Connectivity.config.getCommonConfig().disableLoginLimits)
        {
            return constant;
        }

        return Integer.MAX_VALUE;
    }

    @ModifyConstant(method = "method_56461", constant = @Constant(intValue = 1048576), require = 0)
    private static int modifyLimit2(final int constant)
    {
        if (!Connectivity.config.getCommonConfig().disableLoginLimits)
        {
            return constant;
        }

        return Integer.MAX_VALUE;
    }
}
