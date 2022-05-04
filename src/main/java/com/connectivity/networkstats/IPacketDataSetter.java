package com.connectivity.networkstats;

import net.minecraft.network.FriendlyByteBuf;

public interface IPacketDataSetter
{
    void setData(FriendlyByteBuf buffer);
}
