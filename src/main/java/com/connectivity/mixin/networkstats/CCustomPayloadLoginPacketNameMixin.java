package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.IWrappedPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerboundCustomQueryPacket.class)
public class CCustomPayloadLoginPacketNameMixin implements IWrappedPacket
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
    public void setOrgMsg(final Object name)
    {
        org = name;
    }
}
