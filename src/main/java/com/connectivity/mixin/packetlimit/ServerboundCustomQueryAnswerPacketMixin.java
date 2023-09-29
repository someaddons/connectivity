package com.connectivity.mixin.packetlimit;

import com.connectivity.Connectivity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerboundCustomQueryAnswerPacket.class, priority = 15)
public class ServerboundCustomQueryAnswerPacketMixin
{
    @Inject(method = "readUnknownPayload", at = @At("HEAD"))
      private static void printDebug(final FriendlyByteBuf friendlyByteBuf, final CallbackInfoReturnable<CustomQueryAnswerPayload> cir)
    {
        if (friendlyByteBuf.readableBytes() > 1048576)
        {

        }
    }

    @ModifyConstant(method = "readUnknownPayload", constant = @Constant(intValue = 1048576), require = 0)
    private static int modifyLimit(final int constant)
    {
        if (!Connectivity.config.getCommonConfig().disableLoginLimits)
        {
            return constant;
        }

        return Integer.MAX_VALUE;
    }
}
