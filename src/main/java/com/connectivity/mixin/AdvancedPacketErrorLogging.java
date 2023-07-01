package com.connectivity.mixin;

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

@Mixin(value = Connection.class, priority = 5)
public abstract class AdvancedPacketErrorLogging
{
    @Shadow
    protected abstract void sendPacket(final Packet<?> p_129521_, @Nullable final PacketSendListener p_243246_);

    @Redirect(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;sendPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"), require = 0)
    private void connectivity$logErrorFor(final Connection instance, final Packet<?> packet, final PacketSendListener listener)
    {
        connectivity$wrapSend(packet, listener);
    }

    @Redirect(method = "flushQueue", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;sendPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"), require = 0)
    private void connectivity$logErrorForFlush(final Connection instance, final Packet<?> packet, final PacketSendListener listener)
    {
        connectivity$wrapSend(packet, listener);
    }

    @Unique
    private void connectivity$wrapSend(final Packet<?> packet, final PacketSendListener listener)
    {
        try
        {
            sendPacket(packet, listener);
        }
        catch (Throwable t)
        {
            PacketLogging.logPacket(packet, "threw an error:"+t.getLocalizedMessage());

            throw t;
        }
    }
}
