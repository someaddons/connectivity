package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.google.common.base.Charsets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryAnswerPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryPayload;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerboundCustomQueryAnswerPacket.class)
public class ServerboundCustomQueryAnswerPacketMixin
{
    @Inject(method = "readUnknownPayload", at = @At(value = "INVOKE", target = "Ljava/lang/IllegalArgumentException;<init>(Ljava/lang/String;)V"), cancellable = true)
    private static void onLargePacket(final FriendlyByteBuf data, final CallbackInfoReturnable<CustomQueryAnswerPayload> cir)
    {
        if (Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            data.resetReaderIndex();
            Connectivity.LOGGER.warn("Too big ServerboundCustomQueryAnswerPacket payload data!");
            Connectivity.LOGGER.warn("Data:" + data.toString(Charsets.UTF_8));
            data.resetReaderIndex();
        }

        if (Connectivity.config.getCommonConfig().disableLoginLimits)
        {
            cir.setReturnValue(DiscardedQueryAnswerPayload.INSTANCE);
        }
    }
}
