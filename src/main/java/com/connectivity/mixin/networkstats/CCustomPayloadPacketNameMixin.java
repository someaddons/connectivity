package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerboundCustomPayloadPacket.class)
public class CCustomPayloadPacketNameMixin implements INamedPacket
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
