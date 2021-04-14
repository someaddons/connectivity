package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.google.common.base.Charsets;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.server.SCustomPayloadLoginPacket;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SCustomPayloadLoginPacket.class, priority = 101)
/**
 * Disables client side exception for packet size
 */
public class SCustomPayloadLoginPacketMixin
{
    @Shadow
    private ResourceLocation channel;

    @Shadow
    private PacketBuffer payload;

    @Inject(method = "readPacketData", at = @At(value = "INVOKE", target = "Ljava/io/IOException;<init>(Ljava/lang/String;)V"), cancellable = true, require = 0, expect = 0)
    public void readPacketData(final PacketBuffer buf, final CallbackInfo ci)
    {
        if (Connectivity.config.getCommonConfig().debugPrintMessages.get())
        {
            reportData(buf);
        }
        if (Connectivity.config.getCommonConfig().disableLoginLimits.get())
        {
            payload = new PacketBuffer(buf.readBytes(buf.readableBytes()));
            ci.cancel();
        }
    }

    private void reportData(final PacketBuffer data)
    {
        Connectivity.LOGGER.warn("Too big payload data for: " + channel + " class:" + this.getClass().getSimpleName());
        Connectivity.LOGGER.warn("Data:" + data.toString(Charsets.UTF_8));
    }
}
