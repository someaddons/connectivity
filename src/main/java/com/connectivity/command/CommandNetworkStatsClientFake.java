package com.connectivity.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

/**
 * Print out total network stats
 */
public class CommandNetworkStatsClientFake implements IMCOPCommand
{
    public static final String NETWORKSTATS_CLIENT_FAKE_COMMAND = "/connectivity packetsClient %d %d";

    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        return 0;
    }

    @Override
    public String getName()
    {
        return "packetsClient";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return
          IMCCommand.newLiteral(getName())
            .then(IMCCommand.newArgument("minutes", IntegerArgumentType.integer(1, 400))
              .executes(this::onExecute)
              .then(IMCCommand.newArgument("index", IntegerArgumentType.integer(0, 400)).executes(this::onExecute))).executes(this::onExecute);
    }
}
