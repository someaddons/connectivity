package com.connectivity.mixin;

import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(ClientboundSectionBlocksUpdatePacket.class)
/**
 * Fixes braces to convert the state id to a long before shifting/operating on it, instead of afterwards.
 */
public abstract class SMultiBlockChangePacketMixin
{
    @Shadow
    @Final
    private short[] positions;

    @Shadow
    @Final
    private SectionPos sectionPos;

    @Shadow
    @Final
    private BlockState[] states;

    @Overwrite
    public void write(FriendlyByteBuf buf) throws IOException
    {
        buf.writeLong(this.sectionPos.asLong());
        buf.writeVarInt(this.positions.length);

        for (int i = 0; i < this.positions.length; ++i)
        {
            buf.writeVarLong(((((long) Block.getId(this.states[i])) << 12) | this.positions[i]));
        }
    }
}
