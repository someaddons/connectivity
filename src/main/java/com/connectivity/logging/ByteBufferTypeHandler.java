package com.connectivity.logging;

import com.connectivity.Connectivity;
import com.google.common.base.Charsets;
import com.google.gson.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.lang.reflect.Type;

public class ByteBufferTypeHandler<T>
    implements JsonSerializer<ByteBuf>, JsonDeserializer<ByteBuf>
{
    private static final double HUMA_READABLE_THRESHOLD = 3;

    @Override
    public ByteBuf deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
        return null;
    }

    @Override
    public JsonElement serialize(ByteBuf src, Type typeOfSrc, JsonSerializationContext context)
    {
        final JsonArray result = new JsonArray();
        if (!Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            final JsonElement element = context.serialize("Enable debugPrintMessages to display this data");
            result.add(element);
        }
        else
        {
            final String data = cleanupString(src.toString(Charsets.UTF_8)) + "\n";
            double entropy = calculateEntropy(data);
            if (entropy > HUMA_READABLE_THRESHOLD)
            {
                final JsonElement element = context.serialize(cleanupString(data));
                result.add(element);
            }
            else
            {
                result.add("<partial numeric data>");
                StringBuilder arrayString = new StringBuilder();
                arrayString.append("[");

                byte[] byteData = ByteBufUtil.getBytes(src, 0, src.readableBytes() + src.readerIndex());

                for (int i = 0; i < byteData.length && i < 200; i++)
                {
                    arrayString.append(byteData[i]);
                    if (i < byteData.length - 1)
                    {
                        arrayString.append(", ");
                    }
                }
                arrayString.append("]");
                result.add(arrayString.toString());
            }
        }

        return result;
    }

    public static double calculateEntropy(String input)
    {

        Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        for (char c : input.toCharArray())
        {

            map.put(c, map.get(c) + 1);
        }

        double entropy = 0;
        for (int count : map.values())
        {
            if (count > 0)
            {
                double probability = (double) count / input.length();
                entropy -= probability * (Math.log(probability) / Math.log(2));
            }
        }
        return entropy;
    }

    private String cleanupString(String input)
    {
        StringBuilder readableText = new StringBuilder();

        for (char c : input.toCharArray())
        {
            if (c >= 32 && c <= 126)
            {  // Only keep printable ASCII characters
                readableText.append(c);
            }
        }

        return readableText.toString();
    }
}