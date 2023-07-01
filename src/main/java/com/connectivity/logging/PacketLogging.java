package com.connectivity.logging;

import com.connectivity.Connectivity;
import com.connectivity.networkstats.INamedPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.google.gson.ReflectionAccessFilter.BLOCK_INACCESSIBLE_JAVA;

public class PacketLogging
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
      .addReflectionAccessFilter(BLOCK_INACCESSIBLE_JAVA)
      .disableHtmlEscaping()
      .registerTypeHierarchyAdapter(Optional.class, new GsonOptionalTypeHandler<>())
      .registerTypeHierarchyAdapter(FriendlyByteBuf.class, new ByteBufferTypeHandler<>())
      .create();

    public static void logPacket(final Packet<?> packet)
    {
        logPacket(packet, "");
    }

    public static void logPacket(final Packet<?> packet, String warning)
    {
        String name = packet.getClass().getSimpleName();
        if (packet instanceof INamedPacket)
        {
            if (!((INamedPacket) packet).getName().isEmpty())
            {
                name = ((INamedPacket) packet).getName();
            }
        }

        Connectivity.LOGGER.warn("Packet:" + name+ " " + warning);

        try
        {
            Connectivity.LOGGER.warn("Packet data:\n" + PacketLogging.GSON.toJson(packet));
        }
        catch (Throwable e)
        {
            Connectivity.LOGGER.warn("Failed to print data for packet");
        }
    }
}
