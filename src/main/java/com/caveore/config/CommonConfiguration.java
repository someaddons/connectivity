package com.caveore.config;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

public class CommonConfiguration
{
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> caveblocks;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> excludedOres;

    protected CommonConfiguration(final ForgeConfigSpec.Builder builder)
    {
        builder.push("Ore settings");
        builder.comment(
          "List of blocks to which ores are allowed to spawn next to. This does not override existing spawn restrictions of the ores, as those are restrictions on the block they can spawn instead of.  e.g. format :  [\"minecraft:air\", \"minecraft:cave_air\"]");
        caveblocks = builder.defineList("caveblocks",
          Lists.newArrayList("minecraft:air", "minecraft:cave_air", "minecraft:water", "minecraft:lava"),
          e -> e instanceof String && ((String) e).contains(":"));

        builder.comment("List of excluded ores beeing affected, these are mod-specific. : e.g. format :  [\"mod:orename\", \"minecraft:iron_ore\"]");
        excludedOres = builder.defineList("excludedOres", Collections.emptyList(), e -> e instanceof String && ((String) e).contains(":"));

        // Escapes the current category level
        builder.pop();
    }
}
