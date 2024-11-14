package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketEncoder.class)
public class PacketEncoderMixin
{
    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false, shift = At.Shift.AFTER))
    private void onError(final ChannelHandlerContext p_130545_, final Packet<?> packet, final ByteBuf p_130547_, final CallbackInfo ci)
    {
        PacketLogging.logPacket(packet, "caused an error above, printing name & data");
    }

    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/jfr/JvmProfiler;onPacketSent(Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/network/protocol/PacketType;Ljava/net/SocketAddress;I)V"))
    private void checkSize(final ChannelHandlerContext p_130545_, final Packet packet, final ByteBuf buf, final CallbackInfo ci)
    {
        if (buf.readableBytes() > 8388608 && (Connectivity.config.getCommonConfig().debugPrintMessages || !Connectivity.config.getCommonConfig().disablePacketLimits))
        {
            PacketLogging.logPacket(packet, " being sent is very large size:" + buf.readableBytes() + " bytes");
        }
    }
}
