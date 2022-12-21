package com.connectivity.mixin;

import com.connectivity.event.ClientEventHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ScreenMixin
{
    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    public void on(final String string, final boolean bl, final CallbackInfoReturnable<Boolean> cir)
    {
        ClientEventHandler.on(string, cir);
    }
}
