package com.connectivity.mixin.networkstats;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
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

    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V", at = @At(value = "INVOKE", target = "Ljava/io/IOException;<init>(Ljava/lang/String;)V"))
    public void onNoPacket(final ChannelHandlerContext j, final Packet<?> packet, final ByteBuf friendlybytebuf, final CallbackInfo ci)
    {
        String name = packet.getClass().getSimpleName();
        if (packet instanceof INamedPacket)
        {
            if (!((INamedPacket) packet).getName().isEmpty())
            {
                name = ((INamedPacket) packet).getName();
            }
        }

        Connectivity.LOGGER.warn("Packet not registered: " + name);
        PacketLogging.logPacket(packet);
    }

    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void onError(final ChannelHandlerContext p_130545_, final Packet<?> packet, final ByteBuf p_130547_, final CallbackInfo ci)
    {
        PacketLogging.logPacket(packet, "caused an error above, printing name & data");
    }
}
