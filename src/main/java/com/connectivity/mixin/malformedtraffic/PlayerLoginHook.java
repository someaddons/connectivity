package com.connectivity.mixin.malformedtraffic;

import com.connectivity.networkstats.MalformedTrafficTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerLoginPacketListenerImpl.class)
/**
 * Callback for a successful login
 */
public class PlayerLoginHook
{
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void onPlaceNewPlayer(ServerPlayer serverPlayer, CallbackInfo ci)
    {
        MalformedTrafficTracker.onLogin(serverPlayer.connection.connection.channel);
    }
}
