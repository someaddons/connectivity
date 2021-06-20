package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.NetworkStatGatherer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.IPacket;
import net.minecraft.network.NettyPacketEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NettyPacketEncoder.class)
public class NettyPacketEncoderMixin
{
    @Inject(method = "encode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/IPacket;write(Lnet/minecraft/network/PacketBuffer;)V"))
    private void onEncode(final ChannelHandlerContext channelHandlerContext, final IPacket<?> packet, final ByteBuf packetBuffer, final CallbackInfo ci)
    {
        NetworkStatGatherer.add(channelHandlerContext.channel().remoteAddress().toString(), packet.getClass().getSimpleName(), packetBuffer.writerIndex());
    }
}
