package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.channels.ClosedChannelException;

/**
 * Bit crude mixin, to accomodate for krypton's overwrite
 */
@Mixin(value = Connection.class, priority = 5)
public abstract class AdvancedPacketErrorLogging
{
    @WrapOperation(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"), require = 0)
    private void connectivity$logErrorFor(
      final Connection instance,
      final Packet<?> packet,
        final ChannelFutureListener listener,
      final boolean bool,
      final Operation<Void> original)
    {
        connectivity$wrapSend(instance, packet, listener, bool, original);
    }

    @Unique
    private void connectivity$wrapSend(final Connection instance, final Packet<?> packet, final ChannelFutureListener listener, final boolean bool, final Operation<Void> original)
    {
        try
        {
            original.call(instance, packet, listener, bool);
        }
        catch (Throwable t)
        {
            if (!(t instanceof ClosedChannelException) && Connectivity.config.getCommonConfig().debugPrintMessages)
            {
                PacketLogging.logPacket(packet, "threw an error:" + t.getLocalizedMessage());
            }
            throw t;
        }
    }
}
