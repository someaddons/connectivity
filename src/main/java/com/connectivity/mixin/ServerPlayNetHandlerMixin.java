package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerCommonPacketListenerImpl.class, priority = 1001)
/**
 * Sets the timeout constant
 */
public abstract class ServerPlayNetHandlerMixin
{
    @Shadow
    private boolean keepAlivePending;

    @Shadow
    @Final
    public Connection connection;

    @Shadow
    @Final
    private MinecraftServer server;

    @ModifyConstant(method = "keepConnectionAlive", constant = @Constant(longValue = 15000L, ordinal = 0), require = 0, expect = 0)
    public long playTimeout(long old)
    {
        if (!keepAlivePending)
        {
            return 15000L;
        }

        // Should be lower than readtimeouts
        return (Connectivity.config.getCommonConfig().disconnectTimeout * 1000L) - 15000L - 2000L;
    }

    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("RETURN"))
    private void on(final DisconnectionDetails details, final CallbackInfo ci)
    {
        connection.channel().eventLoop().execute(() -> {
            if (connection.channel().isOpen())
            {
                this.connection.disconnect(details.reason());
                this.server.executeBlocking(connection::handleDisconnection);
            }
        });
    }
}
