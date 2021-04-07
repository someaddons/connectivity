package com.caveore.mixin;

import com.caveore.config.ConfigValues;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(OreFeature.class)
public class OreMixin
{
    private boolean isOreBlock = false;

    @Inject(method = "func_207803_a", at = @At("HEAD"))
    private void ongenerate(
      final IWorld worldIn,
      final Random random,
      final OreFeatureConfig config,
      final double p_207803_4_,
      final double p_207803_6_,
      final double p_207803_8_,
      final double p_207803_10_,
      final double p_207803_12_,
      final double p_207803_14_,
      final int p_207803_16_,
      final int p_207803_17_,
      final int p_207803_18_,
      final int p_207803_19_, final int p_207803_20_, final CallbackInfoReturnable<Boolean> cir)
    {
        isOreBlock = config.state.isIn(Tags.Blocks.ORES) && !ConfigValues.excludedBlocks.contains(config.state.getBlock().getRegistryName());
    }

    @Redirect(method = "func_207803_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState ongetBlockState(final IWorld iWorld, final BlockPos pos)
    {
        if (isOreBlock)
        {
            final BlockPos posI = pos.toImmutable();
            for (final Direction dir : Direction.values())
            {
                // Check surroundings
                final BlockState state = iWorld.getBlockState(posI.offset(dir));
                if (state.isIn(Tags.Blocks.ORES) || ConfigValues.allowedBlocks.contains(state.getBlock().getRegistryName()))
                {
                    return iWorld.getBlockState(pos);
                }
            }
        }
        else
        {
            return iWorld.getBlockState(pos);
        }

        return Blocks.AIR.getDefaultState();
    }
}
