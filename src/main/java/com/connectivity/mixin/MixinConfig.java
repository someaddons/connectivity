package com.connectivity.mixin;

import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MixinConfig implements IMixinConfigPlugin
{
    private final static String basePath = "com.connectivity.mixin.";

    @Override
    public void onLoad(final String mixinPackage)
    {
        System.getProperties().setProperty("forge.disablePacketCompressionDebug", "true");
        disabledMixins.put(basePath + "ClientBoundCustomPayloadPacketLMixin", () -> FMLLoader.getLoadingModList().getModFileById("xlpackets") != null);
        disabledMixins.put(basePath + "compat.MinecoloniesPacketNameMixin", () -> FMLLoader.getLoadingModList().getModFileById("minecolonies") == null);
        disabledMixins.put(basePath + "compat.MinecoloniesNetworkChannelMixin", () -> FMLLoader.getLoadingModList().getModFileById("minecolonies") == null);
        disabledMixins.put(basePath + "compat.StructurizeNetworkChannelMixin", () -> FMLLoader.getLoadingModList().getModFileById("structurize") == null);
        disabledMixins.put(basePath + "compat.StructurizePacketNameMixin", () -> FMLLoader.getLoadingModList().getModFileById("structurize") == null);
    }

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    Map<String, Supplier<Boolean>> disabledMixins = new HashMap<>();

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName)
    {
        if (disabledMixins.containsKey(mixinClassName))
        {
            return !disabledMixins.get(mixinClassName).get();
        }

        return true;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets)
    {

    }

    @Override
    public List<String> getMixins()
    {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo)
    {

    }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo)
    {

    }
}
