package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.VarInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = CompressionDecoder.class, priority = 99)
/**
 * Prints out the messages which were too big and disables client side exceptions if enabled
 */
public abstract class CompressionDecoderMixin extends ByteToMessageDecoder
{
    @Shadow
    private int threshold;

    @Shadow
    private boolean validateDecompressed;

    @Unique
    private int size = 0;

    @Unique
    private boolean originalValidate = false;

    @Inject(method = "decode", at = @At("HEAD"))
    private void disabledLimit(final ChannelHandlerContext p_129441_, final ByteBuf p_129442_, final List<Object> p_129443_, final CallbackInfo ci)
    {
        originalValidate = validateDecompressed;
        validateDecompressed = false;
    }

    @Inject(method = "decode", at = @At("RETURN"))
    private void restoreLimit(final ChannelHandlerContext p_129441_, final ByteBuf p_129442_, final List<Object> p_129443_, final CallbackInfo ci)
    {
        validateDecompressed = originalValidate;
    }

    @Redirect(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/VarInt;read(Lio/netty/buffer/ByteBuf;)I"))
    private int saveSize(final ByteBuf buf)
    {
        this.size = VarInt.read(buf);
        return size;
    }

    @Inject(method = "decode", at = @At("RETURN"))
    private void checkSize(final ChannelHandlerContext p_129441_, final ByteBuf p_129442_, final List<Object> list, final CallbackInfo ci)
    {
        if (size < this.threshold)
        {
            if (!Connectivity.config.getCommonConfig().disablePacketLimits)
            {
                printDebug(list);
                throw new DecoderException("Badly compressed packet - size of " + size + " is below server threshold of " + this.threshold);
            }
        }

        if (size > 8388608)
        {
            if (!Connectivity.config.getCommonConfig().disablePacketLimits)
            {
                printDebug(list);
                throw new DecoderException("Badly compressed packet - size of " + size + " is larger than protocol maximum of " + 8388608);
            }
        }

        size = 0;
    }

    @Unique
    private void printDebug(List<Object> decodingResults)
    {
        if (!Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            return;
        }

        Connectivity.LOGGER.error("Received large message, debug print below!");
        Connectivity.LOGGER.error("----BEGIND PRINTING PACKET-----");
        for (int i = 0; i < decodingResults.size(); i++)
        {
            final ByteBuf buf = ((ByteBuf) decodingResults.get(i));
            if (buf == null)
            {
                continue;
            }

            Connectivity.LOGGER.error("Data:");
            PacketLogging.logPacket(buf);
            buf.resetReaderIndex();
        }
        Connectivity.LOGGER.error("----END PRINTING PACKET-----");
    }
}
