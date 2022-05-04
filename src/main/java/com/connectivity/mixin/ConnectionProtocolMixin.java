package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.networkstats.IPacketDataSetter;
import com.google.common.base.Charsets;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Function;

@Mixin(targets = "net.minecraft.network.ConnectionProtocol$PacketSet", remap = true)
/**
 * Bypasses constructor size limits
 */
public class ConnectionProtocolMixin<T extends PacketListener>
{
    @Shadow
    @Final
    public List<Function<FriendlyByteBuf, ? extends Packet<T>>> idToDeserializer;

    @Inject(method = "addPacket", at = @At(value = "HEAD"), cancellable = true)
    public <P extends Packet<T>> void onAdd(
      final Class<P> pClass,
      Function<FriendlyByteBuf, P> byteBufPFunction,
      final CallbackInfoReturnable cir)
    {
        if (pClass == ServerboundCustomQueryPacket.class && Connectivity.config.getCommonConfig().disableLoginLimits)
        {
            byteBufPFunction = buf ->
            {
                int transactionId = buf.readVarInt();
                FriendlyByteBuf data = null;
                if (buf.readBoolean())
                {
                    int bytes = buf.readableBytes();

                    if (bytes < 0 || bytes > 1048576)
                    {
                        if (Connectivity.config.getCommonConfig().debugPrintMessages)
                        {
                            reportData(pClass, buf);
                        }
                    }

                    data = new FriendlyByteBuf(buf.readBytes(bytes));
                }

                return (P) new ServerboundCustomQueryPacket(transactionId, data);
            };
        }

        if (pClass == ClientboundCustomQueryPacket.class && Connectivity.config.getCommonConfig().disableLoginLimits)
        {
            byteBufPFunction = buf ->
            {
                final int transactionId = buf.readVarInt();
                final ResourceLocation identifier = buf.readResourceLocation();
                final int bytes = buf.readableBytes();
                if (bytes >= 0 && bytes <= 1048576)
                {
                    if (Connectivity.config.getCommonConfig().debugPrintMessages)
                    {
                        reportData(pClass, buf);
                    }
                }
                FriendlyByteBuf data = new FriendlyByteBuf(buf.readBytes(bytes));

                return (P) new ClientboundCustomQueryPacket(transactionId, identifier, data);
            };
        }

        if (pClass == ClientboundCustomPayloadPacket.class && Connectivity.config.getCommonConfig().disableLoginLimits)
        {
            byteBufPFunction = buf ->
            {
                final ResourceLocation identifier = buf.readResourceLocation();
                final int bytes = buf.readableBytes();
                if (bytes >= 0 && bytes <= 1048576)
                {
                    if (Connectivity.config.getCommonConfig().debugPrintMessages)
                    {
                        reportData(pClass, buf);
                    }
                }
                final FriendlyByteBuf data = new FriendlyByteBuf(buf.readBytes(bytes));

                ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(identifier, new FriendlyByteBuf(Unpooled.buffer()));
                ((IPacketDataSetter) packet).setData(data);
                return (P) packet;
            };
        }

        {
            idToDeserializer.add(byteBufPFunction);
        }
    }

    private void reportData(final Class pClass, final FriendlyByteBuf data)
    {
        Connectivity.LOGGER.warn("Too big payload data for class:" + pClass.getSimpleName());
        Connectivity.LOGGER.warn("Data:" + data.toString(Charsets.UTF_8));
    }
}
