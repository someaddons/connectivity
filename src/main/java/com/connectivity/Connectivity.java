package com.connectivity;

import com.connectivity.command.*;
import com.connectivity.config.CommonConfiguration;
import com.connectivity.event.ClientEventHandler;
import com.connectivity.event.EventHandler;
import com.cupboard.config.CupboardConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Connectivity implements ModInitializer, ClientModInitializer
{
    public static final String MODID = "connectivity";

    public static final Logger                              LOGGER = LogManager.getLogger();
    public static       CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MODID, new CommonConfiguration());
    public static MinecraftServer server = null;

    public Connectivity()
    {

    }

    @Override
    public void onInitialize()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, commandSelection) -> {
            LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(MODID);
            dispatcher.register(root.then(new CommandNetworkStatsTotal().build()));
            dispatcher.register(root.then(new CommandNetworkStatsPlayers().build()));
            dispatcher.register(root.then(new CommandNetworkStatsSinglePlayer().build()));
            dispatcher.register(root.then(new CommandNetworkStatsClientFake().build()));
            dispatcher.register(root.then(new CommandNetworkStatsPrintPacket().build()));
        });

        ServerTickEvents.START_SERVER_TICK.register(EventHandler::onServerTick);
        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
        LOGGER.info("Connectivity initialized");
    }

    @Override
    public void onInitializeClient()
    {
        ClientTickEvents.START_CLIENT_TICK.register(ClientEventHandler::onClientTick);
    }
}
