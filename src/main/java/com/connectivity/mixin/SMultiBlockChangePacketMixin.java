package com.connectivity.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SMultiBlockChangePacket;
import net.minecraft.util.math.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(SMultiBlockChangePacket.class)
/**
 * Fixes braces to convert the state id to a long before shifting/operating on it, instead of afterwards.
 */
public abstract class SMultiBlockChangePacketMixin
{
    @Shadow
    private short[] positions;

    @Shadow
    private SectionPos sectionPos;

    @Shadow
    private BlockState[] states;

    @Shadow
    private boolean suppressLightUpdates;

    @Overwrite
    public void write(PacketBuffer buf) throws IOException
    {
        buf.writeLong(this.sectionPos.asLong());
        buf.writeBoolean(this.suppressLightUpdates);
        buf.writeVarInt(this.positions.length);

        for (int i = 0; i < this.positions.length; ++i)
        {
            buf.writeVarLong(((((long) Block.getId(this.states[i])) << 12) | this.positions[i]));
        }
    }
}
