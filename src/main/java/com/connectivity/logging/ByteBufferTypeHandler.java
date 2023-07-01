package com.connectivity.logging;

import com.connectivity.Connectivity;
import com.google.common.base.Charsets;
import com.google.gson.*;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Type;

public class ByteBufferTypeHandler<T>
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
        final JsonArray result = new JsonArray();
        if (!Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            final JsonElement element = context.serialize("Enable debugPrintMessages to print this");
            result.add(element);
        }
        else
        {
            src.resetReaderIndex();
            final JsonElement element = context.serialize(src.toString(Charsets.UTF_8));
            result.add(element);
        }

        return result;
    }
}