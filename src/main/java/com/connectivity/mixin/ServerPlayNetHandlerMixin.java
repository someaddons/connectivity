package com.connectivity.mixin;

import com.connectivity.Connectivity;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 1001)
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

    @ModifyConstant(method = "tick", constant = @Constant(longValue = 15000L, ordinal = 0), require = 0, expect = 0)
    public long playTimeout(long old)
    {
        if (!keepAlivePending)
        {
            return 15000L;
        }

        // Should be lower than readtimeouts
        return (Connectivity.config.getCommonConfig().disconnectTimeout * 1000L) - 15000L - 2000L;
    }

    @Inject(method = "disconnect", at = @At("RETURN"))
    private void on(final Component p_9943_, final CallbackInfo ci)
    {
        connection.channel().eventLoop().execute(() -> {
            if (connection.channel().isOpen())
            {
                this.connection.disconnect(p_9943_);
                this.server.executeBlocking(connection::handleDisconnection);
            }
        });
    }
}
