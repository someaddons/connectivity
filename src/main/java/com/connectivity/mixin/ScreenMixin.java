package com.connectivity.mixin;

import com.connectivity.event.ClientEventHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPacketListener.class)
public class ScreenMixin
{
    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    public void on(final String string, final CallbackInfo ci)
    {
        ClientEventHandler.on(string, ci);
    }
}
