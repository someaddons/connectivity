package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
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
            Connectivity.LOGGER.warn("Trying to send too large packet " + msg.getClass().getSimpleName() + " with size: " + cir.getReturnValue().getKey().writerIndex() + " bytes");
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
