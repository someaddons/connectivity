package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.config.CommonConfiguration;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufLimitMixin
{
    @Unique
    private static long prevLimit = 0;

    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;", at = @At("HEAD"))
    private void releaseLimit(final NbtAccounter accounter, final CallbackInfoReturnable<CompoundTag> cir)
    {
        if (Connectivity.config.getCommonConfig().disablePacketLimits)
        {
            prevLimit = accounter.quota;
            accounter.quota = Long.MAX_VALUE;
        }
    }

    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void checkLimit(final NbtAccounter accounter, final CallbackInfoReturnable<CompoundTag> cir)
    {
        if (Connectivity.config.getCommonConfig().debugPrintMessages && accounter.getUsage() > prevLimit)
        {
            Connectivity.LOGGER.warn("Received too large nbt tag:" + cir.getReturnValue(), new Exception("trace"));
        }
    }
}
