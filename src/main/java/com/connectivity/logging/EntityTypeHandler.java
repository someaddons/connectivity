package com.connectivity.logging;

import com.google.common.base.Charsets;
import com.google.gson.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;

import java.lang.reflect.Type;

public class EntityTypeHandler<T>
  implements JsonSerializer<EntityType>, JsonDeserializer<EntityType>
{

    @Override
    public EntityType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException
    {
        return null;
    }

    @Override
    public JsonElement serialize(EntityType src, Type typeOfSrc, JsonSerializationContext context)
    {
        final JsonElement element = context.serialize(src.toString());
        final JsonArray result = new JsonArray();
        result.add(element);
        return result;
    }
}