package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.IWrappedPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerboundCustomPayloadPacket.class)
public class CCustomPayloadPacketNameMixin implements IWrappedPacket
{
    private Object org = null;

    @Override
    public Object getOriginalMsg()
    {
        if (org instanceof IWrappedPacket && ((IWrappedPacket) org).getOriginalMsg() != null)
        {
            return ((IWrappedPacket) org).getOriginalMsg();
        }
        return org;
    }

    @Override
    public void setOrgMsg(final Object org)
    {
        this.org = org;
    }
}
