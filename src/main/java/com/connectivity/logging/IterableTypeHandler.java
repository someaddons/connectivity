package com.connectivity.logging;

import com.google.gson.*;

import java.lang.reflect.Type;

public class IterableTypeHandler<T>
    implements JsonSerializer<Iterable>, JsonDeserializer<Iterable>
{
    @Override
    public Iterable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
        return null;
    }

    @Override
    public JsonElement serialize(Iterable src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonArray array = new JsonArray();
        for (final Object o : src)
        {
            array.add(context.serialize(o));
        }
        return array;
    }
}