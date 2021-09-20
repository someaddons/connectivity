package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import com.connectivity.networkstats.NetworkStatGatherer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketEncoder.class)
public class NettyPacketEncoderMixin
{
    @Inject(method = "encode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;write(Lnet/minecraft/network/FriendlyByteBuf;)V", shift = At.Shift.AFTER))
    private void onEncode(final ChannelHandlerContext channelHandlerContext, final Packet<?> packet, final ByteBuf packetBuffer, final CallbackInfo ci)
    {
        String name = packet.getClass().getSimpleName();
        if (packet instanceof INamedPacket)
        {
            if (!((INamedPacket) packet).getName().isEmpty())
            {
                name = ((INamedPacket) packet).getName();
            }
        }

        NetworkStatGatherer.add(channelHandlerContext.channel().remoteAddress().toString(), name, packetBuffer.writerIndex());
    }
}
