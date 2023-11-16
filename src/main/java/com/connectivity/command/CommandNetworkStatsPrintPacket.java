package com.connectivity.command;

import com.connectivity.networkstats.NetworkStatGatherer;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

/**
 * Print the biggest packet of a given type
 */
public class CommandNetworkStatsPrintPacket implements IMCOPCommand
{
    public static final String NETWORKSTATS_PRINT_PACKET_COMMAND = "/connectivity printpacket %s";

    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final CommandSourceStack source = context.getSource();
        NetworkStatGatherer.reportStatsSummary(source, 5, 0);
        return 0;
    }

    @Override
    public String getName()
    {
        return "printpacket";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return
          IMCCommand.newLiteral(getName())
            .then(IMCCommand.newArgument("name", StringArgumentType.word())
              .executes(this::executeWithMinutes));
    }

    private int executeWithMinutes(final CommandContext<CommandSourceStack> context)
    {
        final CommandSourceStack source = context.getSource();
        if (!checkPreCondition(context))
        {
            return 0;
        }
        NetworkStatGatherer.printPacketsFittingName(source, StringArgumentType.getString(context, "name"));

        return 0;
    }
}
