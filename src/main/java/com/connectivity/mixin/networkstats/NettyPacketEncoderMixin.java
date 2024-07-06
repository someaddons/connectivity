package com.connectivity.mixin.networkstats;

import com.connectivity.logging.PacketLogging;
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
    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onEncode(final ChannelHandlerContext channelHandlerContext, final Packet<?> packet, final ByteBuf packetBuffer, final CallbackInfo ci)
    {
        NetworkStatGatherer.add(channelHandlerContext.channel().remoteAddress().toString(), packet, packetBuffer.writerIndex());
    }

    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private void onError(final ChannelHandlerContext p_130545_, final Packet<?> packet, final ByteBuf p_130547_, final CallbackInfo ci)
    {
        PacketLogging.logPacket(packet, "caused an error above, printing name & data");
    }
}
