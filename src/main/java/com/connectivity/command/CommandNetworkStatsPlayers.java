package com.connectivity.command;

import com.connectivity.networkstats.NetworkStatGatherer;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

/**
 * Print out total network stats
 */
public class CommandNetworkStatsPlayers implements IMCOPCommand
{
    public static final String NETWORKSTATS_PLAYER_SUMMARY_COMMAND = "/connectivity packetsAllPlayers %d %d";

    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final CommandSourceStack source = context.getSource();
        NetworkStatGatherer.reportAllPlayerSummary(source, 5, 0);
        return 0;
    }

    @Override
    public String getName()
    {
        return "packetsAllPlayers";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return
          IMCCommand.newLiteral(getName())
            .then(IMCCommand.newArgument("minutes", IntegerArgumentType.integer(1, 400))
              .executes(this::executeWithMinutes)
              .then(IMCCommand.newArgument("index", IntegerArgumentType.integer(0, 400)).executes(this::executeWithMinutesandIndex)))
            .executes(this::checkPreConditionAndExecute);
    }

    private int executeWithMinutes(final CommandContext<CommandSourceStack> context)
    {
        final CommandSourceStack source = context.getSource();
        if (!checkPreCondition(context))
        {
            return 0;
        }
        NetworkStatGatherer.reportAllPlayerSummary(source, IntegerArgumentType.getInteger(context, "minutes"), 0);

        return 0;
    }

    private int executeWithMinutesandIndex(final CommandContext<CommandSourceStack> context)
    {
        final CommandSourceStack source = context.getSource();
        if (!checkPreCondition(context))
        {
            return 0;
        }
        NetworkStatGatherer.reportAllPlayerSummary(source, IntegerArgumentType.getInteger(context, "minutes"), IntegerArgumentType.getInteger(context, "index"));

        return 0;
    }
}
