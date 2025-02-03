package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class CCustomPayloadPacketNameMixin implements INamedPacket
{
    @Shadow
    public abstract PacketType<ServerboundCustomPayloadPacket> type();

    @Shadow
    public abstract CustomPacketPayload payload();

    private transient String packetName = "";

    @Override
    public String getName()
    {
        if (packetName.isEmpty())
        {
            return this.payload().type().id().toString();
        }
        return packetName;
    }

    @Override
    public void setName(final String name)
    {
        packetName = name;
    }
}
