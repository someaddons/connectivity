package com.connectivity.mixin.malformedtraffic;

import com.connectivity.networkstats.MalformedTrafficTracker;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerLoginPacketListenerImpl.class)
/**
 * Callback for a successful login
 */
public class PlayerLoginHook
{
    @Shadow @Final public Connection connection;

    @Inject(method = "finishLoginAndWaitForClient", at = @At("TAIL"))
    private void onPlaceNewPlayer(final GameProfile gameProfile, final CallbackInfo ci)
    {
        MalformedTrafficTracker.onLogin(connection.channel);
    }
}
