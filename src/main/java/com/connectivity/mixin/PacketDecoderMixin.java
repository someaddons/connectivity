package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
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
    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/jfr/JvmProfiler;onPacketReceived(IILjava/net/SocketAddress;I)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void checkSize(
        final ChannelHandlerContext p_130535_,
        final ByteBuf p_130536_,
        final List<Object> p_130537_,
        final CallbackInfo ci,
        final int size,
        final FriendlyByteBuf friendlybytebuf,
        final int packetID,
        final Packet packet)
    {
        if (size > 8388608 && (Connectivity.config.getCommonConfig().debugPrintMessages || !Connectivity.config.getCommonConfig().disablePacketLimits))
        {
            PacketLogging.logPacket(packet, " with packetid:" + packetID + " received has too large size:" + size + " bytes");
        }
    }

    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Ljava/io/IOException;<init>(Ljava/lang/String;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onDecode(
        final ChannelHandlerContext p_130535_,
        final ByteBuf buffer,
        final List<Object> p_130537_,
        final CallbackInfo ci,
        final int size,
        final FriendlyByteBuf friendlybytebuf,
        final int packetID,
        final Packet packet)
        throws IOException
    {
        PacketLogging.logPacket(packet, " id " + packetID + " larger than expected error detected, printing packet and buffer. Stacktrace gets logged after this");

        final boolean prev = Connectivity.config.getCommonConfig().debugPrintMessages;
        Connectivity.config.getCommonConfig().debugPrintMessages = true;
        PacketLogging.logPacket(buffer.copy(buffer.readerIndex(), buffer.readableBytes()), " id " + packetID + " data of " + buffer.readableBytes() + " extra bytes: ");
        Connectivity.config.getCommonConfig().debugPrintMessages = prev;

        throw new IOException("Packet " + p_130535_.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId() + "/" + packetID + " (" + packet.getClass().getSimpleName()
            + ") was larger than I expected, found " + friendlybytebuf.readableBytes() + " bytes extra whilst reading packet " + packetID);
    }

    @Redirect(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ConnectionProtocol;createPacket(Lnet/minecraft/network/protocol/PacketFlow;ILnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/Packet;"))
    private Packet<?> checkDecodingError(final ConnectionProtocol instance, final PacketFlow packetFlow, final int packetID, final FriendlyByteBuf buf)
    {
        try
        {
            return instance.createPacket(packetFlow, packetID, buf);
        }
        catch (Throwable t)
        {
            if (Connectivity.config.getCommonConfig().debugPrintMessages)
            {
                int prevIndex = buf.readerIndex();
                buf.resetReaderIndex();

                String name = "unknown";
                for (var entry : instance.flows.get(packetFlow).classToId.object2IntEntrySet())
                {
                    if (entry.getIntValue() == packetID)
                    {
                        name = entry.getKey().toString();
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
            }
            throw t;
        }
    }
}
