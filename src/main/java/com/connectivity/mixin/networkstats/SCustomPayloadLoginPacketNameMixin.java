package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundCustomQueryPacket.class)
public abstract class SCustomPayloadLoginPacketNameMixin implements INamedPacket
{
    @Shadow
    public abstract PacketType<ClientboundCustomQueryPacket> type();

    @Shadow
    public abstract CustomQueryPayload payload();

    private transient String packetName = "";

    @Override
    public String getName()
    {
        if (packetName.isEmpty())
        {
            return this.payload().id().toString();
        }
        return packetName;
    }

    @Override
    public void setName(final String name)
    {
        packetName = name;
    }
}
