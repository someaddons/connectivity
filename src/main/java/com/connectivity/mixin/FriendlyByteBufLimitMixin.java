package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.networkstats.IModifyAbleNbtAccounter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufLimitMixin
{
    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;", at = @At("HEAD"))
    private void releaseLimit(final NbtAccounter accounter, final CallbackInfoReturnable<CompoundTag> cir)
    {
        if (Connectivity.config.getCommonConfig().disablePacketLimits && accounter instanceof IModifyAbleNbtAccounter)
        {
            ((IModifyAbleNbtAccounter) accounter).setQuota(Long.MAX_VALUE);
        }
    }

    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void checkLimit(final NbtAccounter accounter, final CallbackInfoReturnable<CompoundTag> cir)
    {
        if (accounter instanceof IModifyAbleNbtAccounter && Connectivity.config.getCommonConfig().debugPrintMessages
            && accounter.getUsage() > ((IModifyAbleNbtAccounter) accounter).getOriginalQuota())
        {
            Connectivity.LOGGER.warn("Received too large nbt tag:" + cir.getReturnValue(), new Exception("trace"));
        }
    }
}
