package com.connectivity.mixin;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.IdMapper;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(ClientboundSectionBlocksUpdatePacket.class)
/**
 * Fixes braces to convert the state id to a long before shifting/operating on it, instead of afterwards.
 */
public abstract class SMultiBlockChangePacketMixinPolyCompat
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

    @Shadow
    @Final
    private boolean suppressLightUpdates;

    @Overwrite
    public void write(FriendlyByteBuf buf) throws IOException
    {
        buf.writeLong(this.sectionPos.asLong());
        buf.writeBoolean(this.suppressLightUpdates);
        buf.writeVarInt(this.positions.length);

        for (int i = 0; i < this.positions.length; ++i)
        {
            buf.writeVarLong(((((long) Block.getId(PolymerBlockUtils.getPolymerBlockState(this.states[i], PolymerUtils.getPlayer()))) << 12) | this.positions[i]));
        }
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/IdMapper;byId(I)Ljava/lang/Object;"), require = 0)
    private Object polymer_decodeState(final IdMapper instance, final int index)
    {
        return index > 0 ? InternalClientRegistry.decodeState(index) : instance.getId(index);
    }

    @Environment(EnvType.CLIENT)
    @ModifyArg(method = "runUpdates", at = @At(value = "INVOKE", target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"), index = 1)
    private Object polymer_replaceState(Object obj)
    {
        return obj != null ? PolymerBlockUtils.getPolymerBlockState((BlockState) obj, ClientUtils.getPlayer()) : null;
    }
}
