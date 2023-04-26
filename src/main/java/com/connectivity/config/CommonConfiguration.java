package com.connectivity.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfiguration
{
    public final ForgeConfigSpec.BooleanValue disableLoginLimits;
    public final ForgeConfigSpec.BooleanValue disablePacketLimits;
    public final ForgeConfigSpec.BooleanValue debugPrintMessages;
    public final ForgeConfigSpec.BooleanValue showFullResourceLocationException;
    public final ForgeConfigSpec.IntValue     logintimeout;
    public final ForgeConfigSpec.IntValue     packetHistoryMinutes;
    public final ForgeConfigSpec.IntValue     disconnectTimeout;

    public final ForgeConfigSpec ForgeConfigSpecBuilder;

    protected CommonConfiguration(final ForgeConfigSpec.Builder builder)
    {
        builder.push("Connectivity settings");

        builder.comment("Should login packet size limits be disabled? Error:(IOException(\"Payload may not be larger than 1048576 bytes\")) default:true");
        disableLoginLimits = builder.define("disableLoginLimits", true);

        builder.comment("Should play packet size limits be disabled? Error:(Badly compressed packet) default:true");
        disablePacketLimits = builder.define("disablePacketLimits", true);

        builder.comment("Enable addition debug logging for networking errors. default:false");
        debugPrintMessages = builder.define("debugPrintMessages", false);

        builder.comment("Set the max login timeout in ticks. 20 ticks = 1 sec, default = 120 seconds");
        logintimeout = builder.defineInRange("logintimeout", 2400, 600, 20000);

        builder.comment("Set the ingame disconnect timeout for disconnecting players. Default = 60sec");
        disconnectTimeout = builder.defineInRange("disconnectTimeout", 60, 15, 400);

        builder.comment("Set the amount of minutes for which network packet history data is saved. Default = 5 minutes");
        packetHistoryMinutes = builder.defineInRange("packetHistoryMinutes", 5, 1, 400);

        builder.comment("Enable to see the full log output for all resource location exceptions. Default = false");
        showFullResourceLocationException = builder.define("showFullResourceLocationException", false);

        // Escapes the current category level
        builder.pop();
        ForgeConfigSpecBuilder = builder.build();
    }
}
