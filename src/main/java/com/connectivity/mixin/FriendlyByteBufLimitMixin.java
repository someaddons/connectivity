package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.networkstats.IModifyAbleNbtAccounter;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufLimitMixin
{
    @Inject(method = "readNbt(Lio/netty/buffer/ByteBuf;Lnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/Tag;", at = @At("HEAD"))
    private static void releaseLimit(final ByteBuf buf, final NbtAccounter accounter, final CallbackInfoReturnable<Tag> cir)
    {
        if (Connectivity.config.getCommonConfig().disablePacketLimits)
        {
            ((IModifyAbleNbtAccounter) accounter).setQuota(Long.MAX_VALUE);
        }
    }

    @Inject(method = "readNbt(Lio/netty/buffer/ByteBuf;Lnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/Tag;", at = @At("RETURN"))
    private static void checkLimit(final ByteBuf buf, final NbtAccounter accounter, final CallbackInfoReturnable<Tag> cir)
    {
        if (Connectivity.config.getCommonConfig().debugPrintMessages && accounter.getUsage() > ((IModifyAbleNbtAccounter) accounter).getOriginalQuota())
        {
            Connectivity.LOGGER.warn("Received too large nbt tag:" + cir.getReturnValue(), new Exception("trace"));
        }
    }
}
