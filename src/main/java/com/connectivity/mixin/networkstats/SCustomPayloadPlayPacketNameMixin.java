package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class SCustomPayloadPlayPacketNameMixin implements INamedPacket
{
    @Shadow
    public abstract PacketType<ClientboundCustomPayloadPacket> type();

    private transient String packetName = "";

    @Override
    public String getName()
    {
        if (packetName.isEmpty())
        {
            return this.type().id().toString();
        }
        return packetName;
    }

    @Override
    public void setName(final String name)
    {
        packetName = name;
    }
}
