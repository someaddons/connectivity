package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class CCustomPayloadPacketNameMixin implements INamedPacket
{
    @Shadow
    public abstract ResourceLocation getIdentifier();

    private String packetName = "";

    @Override
    public String getName()
    {
        if (packetName.isEmpty())
        {
            return getIdentifier().toString();
        }
        return packetName;
    }

    @Override
    public void setName(final String name)
    {
        packetName = name;
    }
}
