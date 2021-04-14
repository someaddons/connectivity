package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.simple.SimpleChannel;
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
    private <MSG> void test(final MSG msg, final CallbackInfoReturnable<Pair<PacketBuffer, Integer>> cir)
    {
        if (cir.getReturnValue().getKey().writerIndex() > 1048576 && Connectivity.config.getCommonConfig().debugPrintMessages.get())
        {
            Connectivity.LOGGER.warn("Trying to send too large packet " + msg.getClass().getSimpleName() + " with size: " + cir.getReturnValue().getKey().writerIndex() + " bytes");
        }
    }
}
