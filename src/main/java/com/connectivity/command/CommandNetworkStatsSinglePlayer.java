package com.connectivity.command;

import com.connectivity.Connectivity;
import com.connectivity.networkstats.NetworkStatGatherer;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Print out total network stats
 */
public class CommandNetworkStatsSinglePlayer implements IMCOPCommand
{
    public static final String NETWORKSTATS_SINGLE_PLAYER_COMMAND = "/connectivity packetsPlayer %s %d %d";

    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final CommandSourceStack source = context.getSource();
        Collection<GameProfile> profiles = new ArrayList<>();
        try
        {
            profiles = GameProfileArgument.getGameProfiles(context, "player");
        }
        catch (CommandSyntaxException e)
        {
            Connectivity.LOGGER.warn("Player parameter error:", e);
        }

        NetworkStatGatherer.reportPlayerSummary(source, context.getSource().getServer().getPlayerList().getPlayer(profiles.iterator().next().getId()), 5, 0);
        return 0;
    }

    @Override
    public String getName()
    {
        return "packetsPlayer";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return
          IMCCommand.newLiteral(getName())
            .then(IMCCommand.newArgument("player", GameProfileArgument.gameProfile()).executes(this::checkPreConditionAndExecute)
              .then(IMCCommand.newArgument("minutes", IntegerArgumentType.integer(1, 400))
                .executes(this::executeWithMinutes)
                .then(IMCCommand.newArgument("index", IntegerArgumentType.integer(0, 400)).executes(this::executeWithMinutesandIndex))));
    }

    private int executeWithMinutes(final CommandContext<CommandSourceStack> context)
    {
        final CommandSourceStack source = context.getSource();
        if (!checkPreCondition(context))
        {
            return 0;
        }

        Collection<GameProfile> profiles = new ArrayList<>();
        try
        {
            profiles = GameProfileArgument.getGameProfiles(context, "player");
        }
        catch (CommandSyntaxException e)
        {
            Connectivity.LOGGER.warn("Player parameter error:", e);
        }

        NetworkStatGatherer.reportPlayerSummary(source,
          context.getSource().getServer().getPlayerList().getPlayer(profiles.iterator().next().getId()),
          IntegerArgumentType.getInteger(context, "minutes"),
          0);

        return 0;
    }

    private int executeWithMinutesandIndex(final CommandContext<CommandSourceStack> context)
    {
        final CommandSourceStack source = context.getSource();
        if (!checkPreCondition(context))
        {
            return 0;
        }

        Collection<GameProfile> profiles = new ArrayList<>();
        try
        {
            profiles = GameProfileArgument.getGameProfiles(context, "player");
        }
        catch (CommandSyntaxException e)
        {
            Connectivity.LOGGER.warn("Player parameter error:", e);
        }

        NetworkStatGatherer.reportPlayerSummary(source,
          context.getSource().getServer().getPlayerList().getPlayer(profiles.iterator().next().getId()),
          IntegerArgumentType.getInteger(context, "minutes"),
          IntegerArgumentType.getInteger(context, "index"));

        return 0;
    }
}
