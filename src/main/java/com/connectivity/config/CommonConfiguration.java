package com.connectivity.config;

import com.cupboard.config.ICommonConfig;
import com.google.gson.JsonObject;

public class CommonConfiguration implements ICommonConfig
{
    public boolean disableLoginLimits                = true;
    public boolean disablePacketLimits               = true;
    public boolean debugPrintMessages                = false;
    public boolean showFullResourceLocationException = false;
    public int     logintimeout                      = 2400;
    public int     packetHistoryMinutes              = 5;
    public int     disconnectTimeout                 = 60;

    public CommonConfiguration()
    {
    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:", "Should login packet size limits be disabled? Error:(IOException(\"Payload may not be larger than 1048576 bytes\")) default:true");
        entry.addProperty("disableLoginLimits", disableLoginLimits);
        root.add("disableLoginLimits", entry);

        final JsonObject entry2 = new JsonObject();
        entry2.addProperty("desc:", "Should play packet size limits be disabled? Error:(Badly compressed packet) default:true");
        entry2.addProperty("disablePacketLimits", disablePacketLimits);
        root.add("disablePacketLimits", entry2);

        final JsonObject entry3 = new JsonObject();
        entry3.addProperty("desc:", "Enable addition debug logging for networking errors. default:false");
        entry3.addProperty("debugPrintMessages", debugPrintMessages);
        root.add("debugPrintMessages", entry3);

        //TODO: one in ticks one in seconds?
        final JsonObject entry4 = new JsonObject();
        entry4.addProperty("desc:", "Set the max login timeout in ticks. 20 ticks = 1 sec, default = 2400 ticks");
        entry4.addProperty("logintimeout", logintimeout);
        root.add("logintimeout", entry4);

        final JsonObject entry5 = new JsonObject();
        entry5.addProperty("desc:", "Set the ingame disconnect timeout for disconnecting players. Default = 60sec");
        entry5.addProperty("disconnectTimeout", disconnectTimeout);
        root.add("disconnectTimeout", entry5);

        final JsonObject entry6 = new JsonObject();
        entry6.addProperty("desc:", "Set the amount of minutes for which network packet history data is saved. Default = 5 minutes");
        entry6.addProperty("packetHistoryMinutes", packetHistoryMinutes);
        root.add("packetHistoryMinutes", entry6);

        final JsonObject entry7 = new JsonObject();
        entry7.addProperty("desc:", "Enable to see the full log output for all resource location exceptions. Default = false");
        entry7.addProperty("showFullResourceLocationException", showFullResourceLocationException);
        root.add("showFullResourceLocationException", entry7);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        disableLoginLimits = data.get("disableLoginLimits").getAsJsonObject().get("disableLoginLimits").getAsBoolean();
        disablePacketLimits = data.get("disablePacketLimits").getAsJsonObject().get("disablePacketLimits").getAsBoolean();
        debugPrintMessages = data.get("debugPrintMessages").getAsJsonObject().get("debugPrintMessages").getAsBoolean();
        showFullResourceLocationException = data.get("showFullResourceLocationException").getAsJsonObject().get("showFullResourceLocationException").getAsBoolean();
        logintimeout = data.get("logintimeout").getAsJsonObject().get("logintimeout").getAsInt();
        disconnectTimeout = data.get("disconnectTimeout").getAsJsonObject().get("disconnectTimeout").getAsInt();
        packetHistoryMinutes = data.get("packetHistoryMinutes").getAsJsonObject().get("packetHistoryMinutes").getAsInt();
    }
}
