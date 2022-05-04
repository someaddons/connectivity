package com.connectivity.mixin.networkstats;

import com.connectivity.networkstats.IChannelGetter;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Connection.class)
public class ConnectionMixin implements IChannelGetter
{
    @Shadow
    private Channel channel;

    @Override
    public Channel getChannel()
    {
        return channel;
    }
}
