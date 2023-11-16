package com.connectivity.logging;

import com.connectivity.Connectivity;
import com.google.common.base.Charsets;
import com.google.gson.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

public class FriendlyBufferTypeHandler<T>
  implements JsonSerializer<FriendlyByteBuf>, JsonDeserializer<FriendlyByteBuf>
{

    @Override
    public FriendlyByteBuf deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException
    {
        return null;
    }

    @Override
    public JsonElement serialize(FriendlyByteBuf src, Type typeOfSrc, JsonSerializationContext context)
    {
        /*
        final JsonArray result = new JsonArray();
        if (!Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            final JsonElement element = context.serialize("Enable debugPrintMessages to print this");
            result.add(element);
        }
        else
        {
            Pattern pattern = Pattern.compile("(?<=\\d),\\d+");
            context.serialize("Printing reduced bytebuffer data:");
            // Remove matching occurrences
            src.resetReaderIndex();
            final JsonElement element = context.serialize(pattern.matcher(src.toString(Charsets.UTF_8)).replaceAll("") + "\n");
            result.add(element);
            src.resetReaderIndex();
        }*/


        return context.serialize(src.touch());
    }
}