package com.connectivity.networkstats;

/**
 * named packets
 */
public interface IWrappedPacket
{
    public Object getOriginalMsg();

    public void setOrgMsg(final Object name);
}
