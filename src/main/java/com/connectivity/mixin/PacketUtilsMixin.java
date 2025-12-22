package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import net.minecraft.CrashReport;
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
    @Inject(method = "fillCrashReport",
      at = @At(value = "HEAD"))
    private static <T extends PacketListener> void onError(final CrashReport crashReport, final T packetListener, final Packet<T> packet, final CallbackInfo ci)
    {
        if (Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            PacketLogging.logPacket(packet, " caused an error during handling, printing data:");
        }
    }
}
