package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerboundCustomQueryAnswerPacket.class)
public abstract class CCustomPayloadLoginPacketNameMixin implements INamedPacket
{
    @Shadow
    public abstract PacketType<ServerboundCustomQueryAnswerPacket> type();

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
