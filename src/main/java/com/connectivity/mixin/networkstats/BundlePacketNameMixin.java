package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.INamedPacket;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BundlePacket.class)
public abstract class BundlePacketNameMixin implements INamedPacket
{
    @Shadow
    @Final
    private Iterable<Packet<?>> packets;

    @Shadow
    public abstract PacketType<?> type();

    @Override
    public String getName()
    {
        String name = type().id().toString() + " with internal packets: ";

        for (final Packet packet : packets)
        {
            name += " " + packet.type().id().toString();
        }

        return name;
    }

    @Override
    public void setName(final String name)
    {

    }
}
