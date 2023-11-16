package com.connectivity.logging;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GsonErrorHandling implements TypeAdapterFactory
{
    private Map<Integer, Object> currentlySerializing = new HashMap<>();

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type)
    {
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        TypeAdapter<T> customAdapter = new TypeAdapter<T>()
        {
            @Override
            public void write(JsonWriter out, T value) throws IOException
            {
                try
                {
                    if (!(value instanceof String) &&
                          value != null &&
                          !(value instanceof Collection) &&
                          !(value instanceof Map) &&
                          !(value instanceof ByteBuf) &&
                          !value.getClass().getName().startsWith("java."))
                    {
                        // Take advantage of existing toString implementations, those help reducing circles aswell
                        final String toString = value.toString();
                        if (!toString.contains(value.getClass().getName() + "@") && countOccurrences(toString, '@') < 4)
                        {
                            gson.getAdapter(String.class).write(out, toString);
                            return;
                        }
                    }

                    // Check for circular reference before writing
                    if (value != null && !value.equals(currentlySerializing.get(System.identityHashCode(value))))
                    {
                        currentlySerializing.put(System.identityHashCode(value), value);
                        delegate.write(out, value);
                        currentlySerializing.remove(System.identityHashCode(value));
                        return;
                    }

                    if (value == null)
                    {
                        delegate.write(out, value);
                    }
                    else
                    {
                        delegate.write(out, null);
                    }
                }
                catch (Exception e)
                {
                    out.value("Error serializing:" + e.getLocalizedMessage());
                }
            }

            @Override
            public T read(JsonReader in) throws IOException
            {
                return delegate.read(in);
            }
        };

        return customAdapter;
    }

    public static long countOccurrences(String input, char target)
    {
        long count = 0;
        for (char c : input.toCharArray())
        {
            if (c == target)
            {
                count++;
            }
        }
        return count;
    }
}

