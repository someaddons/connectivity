package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.event.ClientEventHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageValidator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin
{
    @Redirect(method = "handlePlayerChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/SignedMessageValidator;updateAndValidate(Lnet/minecraft/network/chat/PlayerChatMessage;)Z"))
    private boolean checkMessage(final SignedMessageValidator instance, final PlayerChatMessage playerChatMessage)
    {
        if (!instance.updateAndValidate(playerChatMessage))
        {
            if (Connectivity.config.getCommonConfig().debugPrintMessages)
            {
                final Component message;
                if (!playerChatMessage.filterMask().isEmpty())
                {
                    Component component = playerChatMessage.filterMask().applyWithFormatting(playerChatMessage.signedContent());
                    message = (component != null ? component : Component.empty());
                }
                else
                {
                    message = playerChatMessage.decoratedContent();
                }

                Connectivity.LOGGER.warn("Failed chat message verification for: " + message.getString());
            }
            return Connectivity.config.getCommonConfig().disableChatVerificationDisconnect;
        }

        return true;
    }

    @Inject(method = "sendCommand", at = @At("HEAD"))
    private void onSendCommand(final String command, final CallbackInfo ci)
    {
        ClientEventHandler.on(command);
    }

    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"))
    private void onSendCommand(final String command, final CallbackInfoReturnable<Boolean> cir)
    {
        ClientEventHandler.on(command);
    }
}
