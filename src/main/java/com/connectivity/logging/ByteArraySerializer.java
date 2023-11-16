package com.connectivity.logging;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Type;

public class ByteArraySerializer implements JsonSerializer<byte[]>
{
    @Override
    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context)
    {
        return context.serialize(Unpooled.wrappedBuffer(src));
    }
}