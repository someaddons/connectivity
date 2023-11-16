package com.connectivity.logging;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.reflect.Type;

public class BlockEntityTypeHandler implements JsonSerializer<BlockEntityType>
{
    @Override
    public JsonElement serialize(BlockEntityType src, Type typeOfSrc, JsonSerializationContext context)
    {
        return context.serialize(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(src));
    }
}