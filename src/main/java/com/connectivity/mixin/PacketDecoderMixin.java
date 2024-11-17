package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.IdDispatchCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.ProtocolInfoBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.List;

@Mixin(PacketDecoder.class)
public class PacketDecoderMixin<T extends PacketListener>
{
    @Shadow
    @Final
    private ProtocolInfo<T> protocolInfo;

    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/jfr/JvmProfiler;onPacketReceived(Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/network/protocol/PacketType;Ljava/net/SocketAddress;I)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void checkSize(final ChannelHandlerContext p_130535_, final ByteBuf buf, final List<Object> p_130537_, final CallbackInfo ci, int size, Packet packet)
    {
        if (size > 8388608 && (Connectivity.config.getCommonConfig().debugPrintMessages || !Connectivity.config.getCommonConfig().disablePacketLimits))
        {
            PacketLogging.logPacket(packet, " received is very large size:" + size + " bytes");
        }
    }

    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Ljava/io/IOException;<init>(Ljava/lang/String;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onDecode(final ChannelHandlerContext p_130535_, final ByteBuf buffer, final List<Object> p_130537_, final CallbackInfo ci, int size, Packet packet)
        throws IOException
    {
        PacketLogging.logPacket(packet, " id " + packet.type() + " larger than expected error detected, printing packet and buffer. Stacktrace gets logged after this");
        final boolean prev = Connectivity.config.getCommonConfig().debugPrintMessages;
        Connectivity.config.getCommonConfig().debugPrintMessages = true;
        PacketLogging.logPacket(buffer.copy(buffer.readerIndex(), buffer.readableBytes()), " id " + packet.type() + " data of " + buffer.readableBytes() + " extra bytes: ");
        Connectivity.config.getCommonConfig().debugPrintMessages = prev;

        throw new IOException(
            "Packet "
                + this.protocolInfo.id().id()
                + "/"
                + packet.type()
                + " ("
                + packet.getClass().getSimpleName()
                + ") was larger than I expected, found "
                + buffer.readableBytes()
                + " bytes extra whilst reading packet "
                + packet.type()
        );
    }

    @Redirect(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;decode(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object checkDecodingError(final StreamCodec instance, final Object o, ChannelHandlerContext p_130535_, ByteBuf buf, List<Object> result)
    {
        try
        {
            return instance.decode(o);
        }
        catch (Throwable t)
        {
            int prevIndex = buf.readerIndex();
            buf.resetReaderIndex();
            int codecIndex = VarInt.read(buf);

            String name = "unknown";
            if (protocolInfo instanceof ProtocolInfoBuilder.Implementation implementation)
            {
                if (implementation.codec() instanceof IdDispatchCodec<?, ?, ?> idDispatchCodec)
                {
                    name = protocolInfo.id().id() + "/" + idDispatchCodec.byId.get(codecIndex).type();
                }
            }

            Connectivity.LOGGER.warn("Decoding error for packet:" + name, t);
            Connectivity.LOGGER.warn("<------ Packet Data Export: ------>");
            final boolean prev = Connectivity.config.getCommonConfig().debugPrintMessages;
            Connectivity.config.getCommonConfig().debugPrintMessages = true;
            PacketLogging.logPacket(buf);
            Connectivity.config.getCommonConfig().debugPrintMessages = prev;
            Connectivity.LOGGER.warn("<------ Packet Data Export End: ------>");
            buf.readerIndex(prevIndex);
            throw t;
        }
    }
}
