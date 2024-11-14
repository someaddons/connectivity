package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PacketEncoder.class)
public class PacketEncoderMixin
{
    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false, shift = At.Shift.AFTER))
    private void onError(final ChannelHandlerContext p_130545_, final Packet<?> packet, final ByteBuf p_130547_, final CallbackInfo ci)
    {
        PacketLogging.logPacket(packet, "caused an error above, printing name & data");
    }

    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writerIndex()I", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void checkSize(
        final ChannelHandlerContext p_130545_,
        final Packet<?> packet,
        final ByteBuf buf,
        final CallbackInfo ci,
        final ConnectionProtocol connectionprotocol,
        final int i,
        final FriendlyByteBuf friendlybytebuf,
        final int size)
    {
        if (size > 8388608 && (Connectivity.config.getCommonConfig().debugPrintMessages || !Connectivity.config.getCommonConfig().disablePacketLimits))
        {
            PacketLogging.logPacket(packet, " being sent is too large size:" + size + " bytes");
        }
    }

    @ModifyConstant(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V", constant = @Constant(intValue = 8388608), require = 0)
    private int modifyLimit(final int constant, ChannelHandlerContext p_130545_, Packet<?> p_130546_, ByteBuf p_130547_)
    {
        if (Connectivity.config.getCommonConfig().disablePacketLimits)
        {
            return Integer.MAX_VALUE;
        }

        return constant;
    }
}
