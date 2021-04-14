package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.util.ResourceLocationException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResourceLocationException.class)
public class ResourceLocationExceptionMixin
{
    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"))
    private void onException(final String message, final CallbackInfo ci)
    {
        if (Connectivity.config.getCommonConfig().showFullResourceLocationException.get())
        {
            Connectivity.LOGGER.warn("ResourceLocationException!: " + message, this);
        }
    }
}
