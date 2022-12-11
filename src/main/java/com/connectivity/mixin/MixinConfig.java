package com.connectivity.mixin;

import net.fabricmc.loader.api.FabricLoader;
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
        if (mixinClassName.contains("SMultiBlockChangePacketMixin") && FabricLoader.getInstance().isModLoaded("polymer"))
        {
            return false;
        }

        if (mixinClassName.contains("SMultiBlockChangePacketMixinPolyCompat") && !FabricLoader.getInstance().isModLoaded("polymer"))
        {
            return false;
        }

        if (disabledMixins.containsKey(mixinClassName))
        {
            if (disabledMixins.get(mixinClassName).get())
            {
                return false;
            }
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
