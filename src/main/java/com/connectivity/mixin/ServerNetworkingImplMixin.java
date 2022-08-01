package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.networkstats.IPacketDataSetter;
import com.google.common.base.Charsets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ServerNetworkingImpl.class)
public class ServerNetworkingImplMixin
{
    /**
     * @author Connectivity
     */
    @Overwrite
    public static Packet<?> createPlayC2SPacket(ResourceLocation identifier, FriendlyByteBuf buf)
    {
        final int bytes = buf.readableBytes();
        if (!(bytes >= 0 && bytes <= 1048576))
        {
            if (Connectivity.config.getCommonConfig().debugPrintMessages)
            {
                reportData(ClientboundCustomPayloadPacket.class, buf);
            }
        }

        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(identifier, new FriendlyByteBuf(Unpooled.buffer()));
        ((IPacketDataSetter) packet).setData(buf);

        return new ClientboundCustomPayloadPacket(identifier, buf);
    }

    private static void reportData(final Class pClass, final FriendlyByteBuf data)
    {
        Connectivity.LOGGER.warn("Too big payload data for class:" + pClass.getSimpleName());
        Connectivity.LOGGER.warn("Data:" + data.toString(Charsets.UTF_8));
    }
}
