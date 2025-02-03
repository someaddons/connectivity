package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelChunk.class)
public abstract class ChunkSectionReadSkipMixin extends ChunkAccess
{
    @Shadow
    @Final
    public Level level;

    public ChunkSectionReadSkipMixin(
        final ChunkPos p_187621_,
        final UpgradeData p_187622_,
        final LevelHeightAccessor p_187623_,
        final Registry<Biome> p_187624_,
        final long p_187625_,
        @Nullable final LevelChunkSection[] p_187626_,
        @Nullable final BlendingData p_187627_)
    {
        super(p_187621_, p_187622_, p_187623_, p_187624_, p_187625_, p_187626_, p_187627_);
    }

    @Redirect(method = "replaceWithPacketData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;read(Lnet/minecraft/network/FriendlyByteBuf;)V"), require = 0)
    private void checkReads(final LevelChunkSection instance, final FriendlyByteBuf buf)
    {
        if (buf.readableBytes() == 0)
        {
            if (Connectivity.config.getCommonConfig().debugPrintMessages)
            {
                Connectivity.LOGGER.error(
                    "Tried to read zero bytes into chunk section, possible desync between chunk section counts " + getPos() + " sections:" + sections.length + " level height:"
                        + level.getHeight());
            }
        }
        else
        {
            instance.read(buf);
        }
    }
}
