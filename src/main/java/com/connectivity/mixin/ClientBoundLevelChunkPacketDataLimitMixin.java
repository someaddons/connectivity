package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientboundLevelChunkPacketData.class)
public class ClientBoundLevelChunkPacketDataLimitMixin
{
    @ModifyConstant(method = "<init>(Lnet/minecraft/network/RegistryFriendlyByteBuf;II)V", constant = @Constant(intValue = 2097152), require = 0)
    private int modifyLimit(final int constant)
    {
        if (!Connectivity.config.getCommonConfig().disablePacketLimits)
        {
            return constant;
        }

        return Integer.MAX_VALUE;
    }
}
