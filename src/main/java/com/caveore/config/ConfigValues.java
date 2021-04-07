package com.caveore.config;

import com.caveore.CaveOre;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Holds values of the config
 */
public class ConfigValues
{
    public static Set<ResourceLocation> allowedBlocks  = new HashSet<>();
    public static Set<ResourceLocation> excludedBlocks = new HashSet<>();

    public static void init()
    {
        allowedBlocks = new HashSet<>();
        for (final String data : CaveOre.config.getCommonConfig().caveblocks.get())
        {
            allowedBlocks.add(getResourceLocation(data));
        }

        excludedBlocks = new HashSet<>();
        for (final String data : CaveOre.config.getCommonConfig().excludedOres.get())
        {
            excludedBlocks.add(getResourceLocation(data));
        }
    }

    private static ResourceLocation getResourceLocation(String string) throws ResourceLocationException
    {
        if (string != null && !string.equals(EMPTY))
        {
            String[] split = string.split(":");
            if (split.length == 2)
            {
                return new ResourceLocation(split[0], split[1]);
            }
        }

        throw new ResourceLocationException("Cannot parse:" + string + " to a valid resource location");
    }
}
