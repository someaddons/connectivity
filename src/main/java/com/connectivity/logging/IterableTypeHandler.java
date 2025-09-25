package com.connectivity.logging;

import com.google.gson.*;

import java.lang.reflect.Type;

public class IterableTypeHandler<T>
    implements JsonSerializer<Iterable>, JsonDeserializer<Iterable>
{
    int iteratingdepth = 0;

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
        iteratingdepth++;

        if (iteratingdepth > 4)
        {
            return array;
        }

        for (final Object o : src)
        {
            array.add(context.serialize(o));
        }
        iteratingdepth--;
        return array;
    }
}