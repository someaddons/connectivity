package com.connectivity;

import com.connectivity.command.*;
import com.connectivity.config.CommonConfiguration;
import com.connectivity.event.ClientEventHandler;
import com.connectivity.event.EventHandler;
import com.cupboard.config.CupboardConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Connectivity.MODID)
public class Connectivity
{
    public static final String MODID = "connectivity";

    public static final Logger                              LOGGER = LogManager.getLogger();
    public static       CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MODID, new CommonConfiguration());

    public Connectivity(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::setup);
        NeoForge.EVENT_BUS.register(EventHandler.class);
        NeoForge.EVENT_BUS.addListener(this::onCommandsRegister);
        modEventBus.addListener(this::clientSetup);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event)
    {
        // Side safe client event handler
        NeoForge.EVENT_BUS.register(ClientEventHandler.class);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Connectivity initialized");
    }

    public void onCommandsRegister(final RegisterCommandsEvent event)
    {
        LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(MODID);
        // Adds all command trees to the dispatcher to register the commands.
        event.getDispatcher().register(root.then(new CommandNetworkStatsTotal().build()));
        event.getDispatcher().register(root.then(new CommandNetworkStatsPlayers().build()));
        event.getDispatcher().register(root.then(new CommandNetworkStatsSinglePlayer().build()));
        event.getDispatcher().register(root.then(new CommandNetworkStatsClientFake().build()));
        event.getDispatcher().register(root.then(new CommandNetworkStatsPrintPacket().build()));
    }
}
