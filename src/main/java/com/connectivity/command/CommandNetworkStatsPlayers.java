package com.connectivity.command;

import com.connectivity.networkstats.NetworkStatGatherer;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;

/**
 * Print out total network stats
 */
public class CommandNetworkStatsPlayers implements IMCOPCommand
{
    public static final String NETWORKSTATS_PLAYER_SUMMARY_COMMAND = "/connectivity packetsAllPlayers %d %d";

    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final CommandSource source = context.getSource();
        NetworkStatGatherer.reportAllPlayerSummary(source, 5, 0);
        return 0;
    }

    @Override
    public String getName()
    {
        return "packetsAllPlayers";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return
          IMCCommand.newLiteral(getName())
            .then(IMCCommand.newArgument("minutes", IntegerArgumentType.integer(1, 400))
                    .executes(this::executeWithMinutes)
                    .then(IMCCommand.newArgument("index", IntegerArgumentType.integer(0, 400)).executes(this::executeWithMinutesandIndex)))
            .executes(this::checkPreConditionAndExecute);
    }

    private int executeWithMinutes(final CommandContext<CommandSource> context)
    {
        final CommandSource source = context.getSource();
        if (!checkPreCondition(context))
        {
            return 0;
        }
        NetworkStatGatherer.reportAllPlayerSummary(source, IntegerArgumentType.getInteger(context, "minutes"), 0);

        return 0;
    }

    private int executeWithMinutesandIndex(final CommandContext<CommandSource> context)
    {
        final CommandSource source = context.getSource();
        if (!checkPreCondition(context))
        {
            return 0;
        }
        NetworkStatGatherer.reportAllPlayerSummary(source, IntegerArgumentType.getInteger(context, "minutes"), IntegerArgumentType.getInteger(context, "index"));

        return 0;
    }
}
