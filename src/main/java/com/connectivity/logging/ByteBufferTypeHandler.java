package com.connectivity.logging;

import com.connectivity.Connectivity;
import com.google.common.base.Charsets;
import com.google.gson.*;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

public class ByteBufferTypeHandler<T>
  implements JsonSerializer<ByteBuf>, JsonDeserializer<ByteBuf>
{
    public static final  double  HUMA_READABLE_THRESHOLD = 4;
    private static final Pattern pattern                 = Pattern.compile("(?<=\\d),\\d+");

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
            final JsonElement element = context.serialize("Enable debugPrintMessages to print this");
            result.add(element);
        }
        else
        {
            src.resetReaderIndex();

            String data = cleanupString(src.toString(Charsets.UTF_8)) + "\n";
            double entropy = calculateEntropy(data);
            data = data.replaceAll("\\p{C}", "");
            data = data.replaceAll("\\?", "");
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
                for (int i = 0; i < src.array().length && i < 200; i++)
                {
                    arrayString.append(src.array()[i]);
                    if (i < src.array().length - 1)
                    {
                        arrayString.append(", ");
                    }
                }
                arrayString.append("]");
                result.add(arrayString.toString());
            }

            src.resetReaderIndex();
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

    private String cleanupString(String in)
    {
        in = in.replaceAll("\\\\n", System.getProperty("line.separator"));
        in = in.replaceAll("�", " ");

        if (pattern.matcher(in).find())
        {
            in = "Printing reduced bytebuffer data:" + System.getProperty("line.separator") + in;
        }

        in = pattern.matcher(in).replaceAll("") + System.getProperty("line.separator");
        return in;
    }
}