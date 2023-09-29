package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Bit crude mixin, to accomodate for krypton's overwrite
 */
@Mixin(value = Connection.class, priority = 5)
public abstract class AdvancedPacketErrorLogging
{
    @Shadow
    protected abstract void sendPacket(final Packet<?> p_129521_, @Nullable final PacketSendListener p_243246_, boolean bl);

    @Redirect(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V"), require = 0)
    private void connectivity$logErrorFor(final Connection instance, final Packet<?> packet, PacketSendListener listener, boolean bl)
    {
        if (listener == null && Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            listener = new PacketSendListener()
            {
                public Packet<?> onFailure()
                {
                    PacketLogging.logPacket(packet, "caused an error above, printing name & data");
                    return null;
                }
            };
        }

        connectivity$wrapSend(packet, listener, bl);
    }

    @Unique
    private void connectivity$wrapSend(final Packet<?> packet, final PacketSendListener listener, boolean bl)
    {
        try
        {
            sendPacket(packet, listener, bl);
        }
        catch (Throwable t)
        {
            PacketLogging.logPacket(packet, "threw an error:" + t.getLocalizedMessage());
            throw t;
        }
    }
}
