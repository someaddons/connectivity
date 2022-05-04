package com.connectivity.mixin;

import com.connectivity.networkstats.IPacketDataSetter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundCustomPayloadPacket.class)
public class ClientboundCustomPayloadPacketMixin implements IPacketDataSetter
{
    @Shadow
    @Final
    @Mutable
    private FriendlyByteBuf data;

    @Override
    public void setData(FriendlyByteBuf buffer)
    {
        data = buffer;
    }
}
