package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerboundCustomQueryPacket.class)
public class CCustomPayloadLoginPacketNameMixin implements INamedPacket
{
    private String packetName = "";

    @Override
    public String getName()
    {
        return packetName;
    }

    @Override
    public void setName(final String name)
    {
        packetName = name;
    }
}
