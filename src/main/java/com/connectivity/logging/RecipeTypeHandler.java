package com.connectivity.logging;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.world.item.crafting.Recipe;

import java.lang.reflect.Type;

public class RecipeTypeHandler implements JsonSerializer<Recipe>
{
    @Override
    public JsonElement serialize(Recipe src, Type typeOfSrc, JsonSerializationContext context)
    {
        return context.serialize(src, src.getClass());
    }
}