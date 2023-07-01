package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Logs errors for too large packets
 */
@Mixin(value = SimpleChannel.class, priority = 101)
public class SimpleChannelMixin
{
    @Inject(method = "toBuffer", at = @At("RETURN"), remap = false)
    private <MSG> void toBufferWarning(final MSG msg, final CallbackInfoReturnable<Pair<FriendlyByteBuf, Integer>> cir)
    {
        if (cir.getReturnValue().getKey().writerIndex() > 1048576 && Connectivity.config.getCommonConfig().debugPrintMessages.get())
        {
            if (msg instanceof Packet)
            {
                PacketLogging.logPacket((Packet<?>) msg, "is sending too much data: " + cir.getReturnValue().getKey().writerIndex() + " bytes, max 1048576");
            }
            else
            {
                Connectivity.LOGGER.warn(
                  "Packet " + msg.getClass().getSimpleName() + " is sending too much data: " + cir.getReturnValue().getKey().writerIndex() + " bytes, max 1048576");
            }
        }
    }

    @Inject(method = "toVanillaPacket", at = @At("RETURN"), remap = false)
    public <MSG> void onWrapInVanillaPacket(final MSG message, final NetworkDirection direction, final CallbackInfoReturnable<Packet<?>> cir)
    {
        final Packet vanilla = cir.getReturnValue();

        if (vanilla instanceof INamedPacket)
        {
            ((INamedPacket) vanilla).setName(message.getClass().getSimpleName());
        }
    }
}
