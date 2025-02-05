package com.connectivity.mixin;

import com.connectivity.networkstats.IModifyAbleNbtAccounter;
import net.minecraft.nbt.NbtAccounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NbtAccounter.class)
public class NbtAccounterMixin implements IModifyAbleNbtAccounter
{
    @Shadow
    public long quota;

    @Unique
    private long originalQuota = 0;

    @Unique
    @Override
    public void setQuota(final long newQuota)
    {
        originalQuota = quota;
        quota = newQuota;
    }

    @Unique
    @Override
    public long getOriginalQuota()
    {
        if (originalQuota == 0)
        {
            return quota;
        }

        return originalQuota;
    }
}
