package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.zip.Inflater;

@Mixin(value = CompressionDecoder.class, priority = 99)
/**
 * Prints out the messages which were too big and disables client side exceptions if enabled
 */
public abstract class NettyCompressionDecoderMixin extends ByteToMessageDecoder
{
    @Shadow
    private int      threshold;
    @Shadow
    @Final
    private Inflater inflater;

    @Overwrite
    public void decode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> decoded) throws Exception
    {
        try
        {
            if (byteBuf.readableBytes() > 0)
            {
                FriendlyByteBuf packetbuffer = new FriendlyByteBuf(byteBuf);
                int i = packetbuffer.readVarInt();
                if (i == 0)
                {
                    decoded.add(packetbuffer.readBytes(packetbuffer.readableBytes()));
                }
                else
                {
                    byte[] abyte = new byte[packetbuffer.readableBytes()];
                    packetbuffer.readBytes(abyte);
                    this.inflater.setInput(abyte);
                    byte[] abyte1 = new byte[i];
                    this.inflater.inflate(abyte1);
                    decoded.add(Unpooled.wrappedBuffer(abyte1));
                    this.inflater.reset();

                    if (i < this.threshold)
                    {
                        if (!Connectivity.config.getCommonConfig().disablePacketLimits.get())
                        {
                            throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.threshold);
                        }
                    }

                    if (i > 2097152)
                    {
                        if (!Connectivity.config.getCommonConfig().disablePacketLimits.get())
                        {
                            throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of " + 2097152);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            printDebug(decoded, byteBuf);
            //throw e;
        }
    }

    private void printDebug(List<Object> decodingResults, final ByteBuf rawData)
    {
        if (!Connectivity.config.getCommonConfig().debugPrintMessages.get())
        {
            return;
        }

        Connectivity.LOGGER.error("Received too large message, debug print below!");
        Connectivity.LOGGER.error("----BEGIND PRINTING PACKET-----");
        rawData.resetReaderIndex();
        Connectivity.LOGGER.error("RawData:" + rawData.toString(Charsets.UTF_8));
        Connectivity.LOGGER.error("----END PRINTING PACKET-----");
        Connectivity.LOGGER.error("----BEGIND PRINTING DECODED PACKET RESULTS-----");
        for (int i = 0; i < decodingResults.size(); i++)
        {
            final ByteBuf buf = ((ByteBuf) decodingResults.get(i));
            if (buf == null)
            {
                continue;
            }
            Connectivity.LOGGER.error("Data:");
            Connectivity.LOGGER.error(buf.toString(Charsets.UTF_8));
        }
        Connectivity.LOGGER.error("----END PRINTING DECODED PACKET RESULTS-----");
    }
}
