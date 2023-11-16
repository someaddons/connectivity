package com.connectivity.logging;

import com.connectivity.Connectivity;
import com.google.common.base.Charsets;
import com.google.gson.*;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

public class HolderReferenceTypeHandler<T>
  implements JsonSerializer<Holder.Reference>, JsonDeserializer<Holder.Reference>
{

    @Override
    public Holder.Reference deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException
    {
        return null;
    }

    @Override
    public JsonElement serialize(Holder.Reference src, Type typeOfSrc, JsonSerializationContext context)
    {
        return context.serialize(src.toString());
    }
}