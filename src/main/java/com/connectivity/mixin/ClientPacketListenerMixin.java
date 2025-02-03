package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.event.ClientEventHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin
{
    @Inject(method = "handlePlayerChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;disconnect(Lnet/minecraft/network/chat/Component;)V"), cancellable = true)
    private void checkMessage(final ClientboundPlayerChatPacket playerChatMessage, final CallbackInfo ci)
    {
        if (Connectivity.config.getCommonConfig().debugPrintMessages)
        {
            final Component message;
            if (!playerChatMessage.filterMask().isEmpty())
            {
                Component component = playerChatMessage.filterMask().applyWithFormatting(playerChatMessage.body().content());
                message = (component != null ? component : Component.empty());
            }
            else
            {
                message = playerChatMessage.unsignedContent();
            }

            Connectivity.LOGGER.warn("Failed chat message verification for: " + message.getString());
        }

        if (Connectivity.config.getCommonConfig().disableChatVerificationDisconnect)
        {
            ci.cancel();
        }
    }

    @Inject(method = "sendCommand", at = @At("HEAD"))
    public void on(final String string, final CallbackInfo ci)
    {
        ClientEventHandler.on(string);
    }

    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"))
    private void onSendCommand(final String command, final CallbackInfoReturnable<Boolean> cir)
    {
        ClientEventHandler.on(command);
    }
}
