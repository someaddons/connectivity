package com.connectivity;

import com.connectivity.command.CommandNetworkStatsClientFake;
import com.connectivity.command.CommandNetworkStatsPlayers;
import com.connectivity.command.CommandNetworkStatsSinglePlayer;
import com.connectivity.command.CommandNetworkStatsTotal;
import com.connectivity.config.Configuration;
import com.connectivity.event.ClientEventHandler;
import com.connectivity.event.EventHandler;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Connectivity implements ModInitializer, ClientModInitializer
{
    public static final String MODID = "connectivity";

    public static final Logger        LOGGER = LogManager.getLogger();
    public static       Configuration config = new Configuration();

    public Connectivity()
    {
        config.load();
    }

    @Override
    public void onInitialize()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(MODID);
            dispatcher.register(root.then(new CommandNetworkStatsTotal().build()));
            dispatcher.register(root.then(new CommandNetworkStatsPlayers().build()));
            dispatcher.register(root.then(new CommandNetworkStatsSinglePlayer().build()));
            dispatcher.register(root.then(new CommandNetworkStatsClientFake().build()));
        });

        ServerTickEvents.START_SERVER_TICK.register(EventHandler::onServerTick);
        LOGGER.info("Connectivity initialized");
    }

    @Override
    public void onInitializeClient()
    {
        ClientTickEvents.START_CLIENT_TICK.register(ClientEventHandler::onClientTick);
    }
}
