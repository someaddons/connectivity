package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class SCustomPayloadPlayPacketNameMixin implements INamedPacket
{
    @Shadow
    public abstract CustomPacketPayload payload();

    private String packetName = "";

    @Override
    public String getName()
    {
        if (packetName.isEmpty())
        {
            return payload().id().toString();
        }
        return packetName;
    }

    @Override
    public void setName(final String name)
    {
        packetName = name;
    }
}
