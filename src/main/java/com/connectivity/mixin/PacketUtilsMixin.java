package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketUtils.class)
public class PacketUtilsMixin
{
    @Inject(method = "lambda$ensureRunningOnSameThread$0",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketListener;onPacketError(Lnet/minecraft/network/protocol/Packet;Ljava/lang/Exception;)V", shift = At.Shift.AFTER))
    private static void onError(final PacketListener p_131365_, final Packet packet, final CallbackInfo ci)
    {
        if (Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            PacketLogging.logPacket(packet, " caused an error during handling, printing data:");
        }
    }
}
