package com.connectivity.mixin.compat;

import com.connectivity.networkstats.IWrappedPacket;
import com.ldtteam.structurize.network.NetworkChannel;
import com.ldtteam.structurize.network.messages.IMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = NetworkChannel.class, remap = false)
public class StructurizeNetworkChannelMixin
{
    @Unique
    IMessage message = null;

    @Inject(method = "handleSplitting", at = @At(value = "HEAD"), remap = false)
    private void onHandleSplit(final IMessage msg, final Consumer<IMessage> splitMessageConsumer, final CallbackInfo ci)
    {
        message = msg;
    }

    @Redirect(method = "handleSplitting", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"), remap = false)
    private <T> void onHandleSplit(final Consumer instance, final T t)
    {
        if (t instanceof IWrappedPacket)
        {
            ((IWrappedPacket) t).setOrgMsg(message);
        }

        instance.accept(t);
    }
}
