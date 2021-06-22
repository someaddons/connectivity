package com.connectivity;

import com.connectivity.command.CommandNetworkStatsClientFake;
import com.connectivity.command.CommandNetworkStatsPlayers;
import com.connectivity.command.CommandNetworkStatsSinglePlayer;
import com.connectivity.command.CommandNetworkStatsTotal;
import com.connectivity.config.Configuration;
import com.connectivity.event.ClientEventHandler;
import com.connectivity.event.EventHandler;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Connectivity.MODID)
public class Connectivity
{
    public static final String MODID = "connectivity";

    public static final Logger        LOGGER = LogManager.getLogger();
    public static       Configuration config = new Configuration();

    public Connectivity()
    {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().addListener(this::onCommandsRegister);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event)
    {
        // Side safe client event handler
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventHandler.class);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Connectivity initialized");
    }

    public void onCommandsRegister(final RegisterCommandsEvent event)
    {
        LiteralArgumentBuilder<CommandSource> root = LiteralArgumentBuilder.literal(MODID);
        // Adds all command trees to the dispatcher to register the commands.
        event.getDispatcher().register(root.then(new CommandNetworkStatsTotal().build()));
        event.getDispatcher().register(root.then(new CommandNetworkStatsPlayers().build()));
        event.getDispatcher().register(root.then(new CommandNetworkStatsSinglePlayer().build()));
        event.getDispatcher().register(root.then(new CommandNetworkStatsClientFake().build()));
    }
}
