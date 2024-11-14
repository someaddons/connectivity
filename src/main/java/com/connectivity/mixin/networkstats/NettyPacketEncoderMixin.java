package com.connectivity.mixin.networkstats;

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
    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;write(Lnet/minecraft/network/FriendlyByteBuf;)V", shift = At.Shift.AFTER))
    private void onEncode(final ChannelHandlerContext channelHandlerContext, final Packet<?> packet, final ByteBuf packetBuffer, final CallbackInfo ci)
    {
        NetworkStatGatherer.add(channelHandlerContext.channel().remoteAddress().toString(), packet, packetBuffer.writerIndex());
    }
}
