package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.connectivity.config.ConfigValues.PERCENT_FORMAT;

@Mixin(value = NetworkRegistry.LoginPayload.class, priority = 1001)

/**
 * Log warnings about registry packets becoming large
 */
public class NetworkRegistryMixin
{
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(final PacketBuffer buffer, final ResourceLocation channelName, final String messageContext, final CallbackInfo ci)
    {
        final double percent = (buffer.writerIndex() / 1048576d) * 100;
        if (percent > 20)
        {
            if (percent < 100)
            {
                Connectivity.LOGGER.warn("Login payload for " + messageContext + " is using " + PERCENT_FORMAT.format(percent) + "% of max allowed vanilla size");
            }
            else
            {
                Connectivity.LOGGER.warn("Login payload for " + messageContext + " is using " + PERCENT_FORMAT.format(percent)
                                           + "% of max allowed vanilla size and will cause errors during login if connectivity is not present on client side.");
            }
        }
    }
}
