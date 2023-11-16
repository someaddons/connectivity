package com.connectivity.logging;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class IntArraySerializer implements JsonSerializer<int[]>
{
    private static final int MAX_ARRAY_SIZE_TO_SERIALIZE = 20; // Set your threshold here

    @Override
    public JsonElement serialize(int[] src, Type typeOfSrc, JsonSerializationContext context) {
        if (src.length > MAX_ARRAY_SIZE_TO_SERIALIZE) {
            // Skip serialization and log a message
            return context.serialize("int["+src.length+"] too big to print");
        }

        // Custom serialization logic for int[] array
        StringBuilder arrayString = new StringBuilder();
        arrayString.append("[");
        for (int i = 0; i < src.length; i++) {
            arrayString.append(src[i]);
            if (i < src.length - 1) {
                arrayString.append(", ");
            }
        }
        arrayString.append("]");

        return context.serialize(arrayString.toString());
    }
}