package com.connectivity.mixin.compat;

import com.connectivity.networkstats.IWrappedPacket;
import com.minecolonies.core.network.messages.splitting.SplitPacketMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = SplitPacketMessage.class, remap = false)
public class MinecoloniesPacketNameMixin implements IWrappedPacket
{
    @Unique
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
