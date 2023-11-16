package com.connectivity.logging;

import com.connectivity.Connectivity;
import com.connectivity.networkstats.IWrappedPacket;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Optional;

import static com.google.gson.ReflectionAccessFilter.BLOCK_INACCESSIBLE_JAVA;

public class PacketLogging
{
    private static final Gson getGson()
    {
        return new GsonBuilder().setPrettyPrinting()
          .disableHtmlEscaping()
          .addReflectionAccessFilter(BLOCK_INACCESSIBLE_JAVA)
          .registerTypeAdapterFactory(new GsonErrorHandling())
          .registerTypeHierarchyAdapter(Optional.class, new GsonOptionalTypeHandler<>())
          .registerTypeHierarchyAdapter(EntityType.class, new EntityTypeHandler<>())
          .registerTypeHierarchyAdapter(BlockEntityType.class, new BlockEntityTypeHandler())
          .registerTypeHierarchyAdapter(ByteBuf.class, new ByteBufferTypeHandler<>())
          .registerTypeHierarchyAdapter(FriendlyByteBuf.class, new FriendlyBufferTypeHandler<>())
          .registerTypeHierarchyAdapter(Holder.Reference.class, new HolderReferenceTypeHandler<>())
          .registerTypeHierarchyAdapter(Holder.Reference.class, new HolderReferenceTypeHandler<>())
          .addSerializationExclusionStrategy(new ExclusionStrategy()
          {
              @Override
              public boolean shouldSkipField(final FieldAttributes f)
              {
                  return f.getDeclaredType() instanceof Registry || f.getDeclaredType() instanceof Holder;
              }

              @Override
              public boolean shouldSkipClass(final Class<?> clazz)
              {
                  return clazz.isInstance(Registry.class) || clazz.isInstance(Holder.class);
              }
          })
          .registerTypeHierarchyAdapter(int[].class, new IntArraySerializer())
          .registerTypeHierarchyAdapter(byte[].class, new ByteArraySerializer())
          .registerTypeHierarchyAdapter(double[].class, new DoubleArraySerializer())
          .registerTypeHierarchyAdapter(float[].class, new FloatArraySerializer())
          .registerTypeHierarchyAdapter(long[].class, new LongArraySerializer())
          .registerTypeAdapter(Recipe.class, new RecipeTypeHandler())
          .create();
    }

    public static void logPacket(final Object packet)
    {
        logPacket(packet, "");
    }

    public static void logPacket(final Object packet, String warning)
    {
        Object orgPacket = packet;
        if (packet instanceof IWrappedPacket && ((IWrappedPacket) packet).getOriginalMsg() != null)
        {
            orgPacket = ((IWrappedPacket) packet).getOriginalMsg();
        }

        final String name = orgPacket.getClass().getSimpleName();

        Connectivity.LOGGER.warn("Packet:" + name + " " + warning);

        try
        {
            Connectivity.LOGGER.warn("Packet data:\n" + PacketLogging.getGson().toJson(orgPacket));
        }
        catch (Throwable e)
        {
            // Idea: Save the biggest packet for on demand printing
            Connectivity.LOGGER.warn("Failed to print data for packet", e);
        }
    }
}
