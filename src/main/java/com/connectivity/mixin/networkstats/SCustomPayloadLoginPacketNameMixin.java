package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundCustomQueryPacket.class)
public class SCustomPayloadLoginPacketNameMixin implements INamedPacket
{
    @Shadow
    @Final
    private ResourceLocation identifier;
    private String           packetName = "";

    @Override
    public String getName()
    {
        if (packetName.isEmpty())
        {
            return identifier.toString();
        }
        return packetName;
    }

    @Override
    public void setName(final String name)
    {
        packetName = name;
    }
}
