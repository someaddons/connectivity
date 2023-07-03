package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundCustomPayloadPacket.class)
public class SCustomPayloadPlayPacketNameMixin implements INamedPacket
{
    private String packetName = "";

    @Override
    public String getName()
    {
        if (packetName == null)
        {
            return "";
        }
        return packetName;
    }

    @Override
    public void setName(final String name)
    {
        packetName = name;
    }
}
