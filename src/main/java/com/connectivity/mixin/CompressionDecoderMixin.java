package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import com.connectivity.networkstats.MalformedTrafficTracker;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.minecraft.network.CompressionDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
        if (Connectivity.config.getCommonConfig().disablePacketLimits)
        {
            originalValidate = validateDecompressed;
            validateDecompressed = false;
        }
    }

    @Inject(method = "decode", at = @At("RETURN"))
    private void restoreLimit(final ChannelHandlerContext p_129441_, final ByteBuf p_129442_, final List<Object> p_129443_, final CallbackInfo ci)
    {
        if (Connectivity.config.getCommonConfig().disablePacketLimits)
        {
            validateDecompressed = originalValidate;
        }
    }

    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Lio/netty/handler/codec/DecoderException;<init>(Ljava/lang/String;)V"), remap = false)
    private void onError(final ChannelHandlerContext context, final ByteBuf p_129442_, final List<Object> p_129443_, final CallbackInfo ci)
    {
        printDebug(p_129443_);
    }

    @Unique
    private void printDebug(List<Object> decodingResults)
    {
        if (!Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            return;
        }

        Connectivity.LOGGER.error("Received message causing a decode exception below, printing data:!");
        Connectivity.LOGGER.error("----BEGIND PRINTING PACKET-----");
        for (int i = 0; i < decodingResults.size(); i++)
        {
            final ByteBuf buf = ((ByteBuf) decodingResults.get(i));
            if (buf == null)
            {
                continue;
            }

            Connectivity.LOGGER.error("Data:");
            final boolean prev = Connectivity.config.getCommonConfig().debugPrintMessages;
            Connectivity.config.getCommonConfig().debugPrintMessages = true;
            PacketLogging.logPacket(buf);
            Connectivity.config.getCommonConfig().debugPrintMessages = prev;
            buf.resetReaderIndex();
        }
        Connectivity.LOGGER.error("----END PRINTING PACKET-----");
    }
}
