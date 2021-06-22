package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.login.server.SCustomPayloadLoginPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SCustomPayloadLoginPacket.class)
public class SCustomPayloadLoginPacketNameMixin implements INamedPacket
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
