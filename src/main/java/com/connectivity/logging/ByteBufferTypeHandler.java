package com.connectivity.logging;

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
        src.resetReaderIndex();
        final JsonElement element = context.serialize(src.toString(Charsets.UTF_8));
        final JsonArray result = new JsonArray();
        result.add(element);
        return result;
    }
}