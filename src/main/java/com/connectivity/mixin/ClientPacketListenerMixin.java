package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageValidator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
}
