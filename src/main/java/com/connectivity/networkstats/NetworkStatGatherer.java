package com.connectivity.networkstats;

import com.connectivity.Connectivity;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.connectivity.command.CommandNetworkStatsClientFake.NETWORKSTATS_CLIENT_FAKE_COMMAND;
import static com.connectivity.command.CommandNetworkStatsPlayers.NETWORKSTATS_PLAYER_SUMMARY_COMMAND;
import static com.connectivity.command.CommandNetworkStatsSinglePlayer.NETWORKSTATS_SINGLE_PLAYER_COMMAND;
import static com.connectivity.command.CommandNetworkStatsTotal.NETWORKSTATS_SUMMARY_COMMAND;

public class NetworkStatGatherer
{
    /**
     * Map of connection remote adress to summary of data per packet
     */
    private static volatile Map<String, ConcurrentHashMap<String, PacketData>> connectionPacketData = new ConcurrentHashMap<>();

    /**
     * The maximum of minute data saved
     */
    private static final int recordingDuration = Connectivity.config.getCommonConfig().packetHistoryMinutes.get();

    /**
     * Data snapshots saved every 1 minute
     */
    private static List<Map<String, ConcurrentHashMap<String, PacketData>>> minuteData = new ArrayList<>(recordingDuration);
    static         int                                                      index      = 0;
    static
    {
        for (int i = 0; i < recordingDuration; i++)
        {
            minuteData.add(null);
        }
    }
    /**
     * Add packet type and size to the given address
     *
     * @param remoteAddress
     * @param packetName
     * @param packetSize
     */
    public static void add(final String remoteAddress, final String packetName, final int packetSize)
    {
        connectionPacketData.computeIfAbsent(remoteAddress, remote -> new ConcurrentHashMap<>()).computeIfAbsent(packetName, name -> new PacketData(name)).add(packetSize);
    }

    /**
     * Gather current data into a resultset every minute
     */
    public static void saveData()
    {
        if (++index >= recordingDuration)
        {
            index = 0;
        }

        minuteData.set(index, connectionPacketData);
        connectionPacketData = new ConcurrentHashMap<>();
    }

    /**
     * Packet data gathering, constaints condensed stats for the given packet name
     */
    private static class PacketData
    {
        private PacketData(final String packetName)
        {
            this.packetName = packetName;
        }

        public final String packetName;
        public       int    maxPacketBytes   = 0;
        public       long   totalPacketBytes = 0;
        public       int    packetCount      = 0;

        /**
         * throughput in kb/s
         */
        public double rate = 0;

        void add(int packetSize)
        {
            if (maxPacketBytes < packetSize)
            {
                maxPacketBytes = packetSize;
            }

            packetCount++;

            totalPacketBytes += packetSize;
        }
    }

    public static PlayerPacketDataEntry getDataForPlayer(final ServerPlayerEntity playerEntity, final int minutes)
    {
        final String id = playerEntity.connection.connection.channel().remoteAddress().toString();

        final Map<String, PacketData> gatherMap = new HashMap<>();

        int timeMinutes = minutes;

        for (int i = 0; i < minutes; i++)
        {
            int preIndex = Math.abs((index - i) % recordingDuration);
            final Map<String, ConcurrentHashMap<String, PacketData>> data = minuteData.get(preIndex);
            if (data != null && data.containsKey(id))
            {
                condenseDataToMap(data.get(id).values(), gatherMap);
            }

            if (data == null)
            {
                // Reduce minute count when the minute data does not exist, to keep proper rates
                timeMinutes--;
            }
        }

        final List<PacketData> dataList = new ArrayList<>(gatherMap.values());
        dataList.sort(Comparator.<PacketData>comparingLong(data -> data.totalPacketBytes).reversed());
        calculateThroughput(dataList, timeMinutes);

        long totalBytes = 0;
        double totalRate = 0;
        for (final PacketData entry : dataList)
        {
            totalBytes += entry.totalPacketBytes;
            totalRate += entry.rate;
        }

        return new PlayerPacketDataEntry(dataList, totalBytes, totalRate, playerEntity);
    }

    public static List<PacketData> getDataByPacket(final int minutes)
    {
        final Map<String, PacketData> gatherMap = new HashMap<>();

        int timeMinutes = minutes;
        for (int i = 0; i < minutes; i++)
        {
            int preIndex = Math.abs((index - i) % recordingDuration);
            final Map<String, ConcurrentHashMap<String, PacketData>> data = minuteData.get(preIndex);
            if (data != null)
            {
                for (final Map.Entry<String, ConcurrentHashMap<String, PacketData>> entry : data.entrySet())
                {
                    condenseDataToMap(entry.getValue().values(), gatherMap);
                }
            }
            else
            {
                timeMinutes--;
            }
        }

        final List<PacketData> result = new ArrayList<>(gatherMap.values());
        result.sort(Comparator.<PacketData>comparingLong(data -> data.totalPacketBytes).reversed());
        calculateThroughput(result, timeMinutes);
        return result;
    }

    private static void calculateThroughput(final Collection<PacketData> data, final int minutes)
    {
        if (minutes <= 0)
        {
            return;
        }

        for (final PacketData packetData : data)
        {
            // Rate in kb/s
            packetData.rate = (packetData.totalPacketBytes / (minutes * 60f)) / 1000d;
        }
    }

    /**
     * Summaries the data of same packets in the given map
     *
     * @param insertData
     * @param dataMap
     */
    private static void condenseDataToMap(final Collection<PacketData> insertData, final Map<String, PacketData> dataMap)
    {
        if (insertData == null)
        {
            return;
        }

        for (final PacketData data : insertData)
        {
            if (dataMap.containsKey(data.packetName))
            {
                final PacketData existing = dataMap.get(data.packetName);
                existing.totalPacketBytes += data.totalPacketBytes;
                existing.packetCount += data.packetCount;
                existing.maxPacketBytes = Math.max(existing.maxPacketBytes, data.maxPacketBytes);
            }
            else
            {
                // Save new data to not alter original data
                final PacketData newData = new PacketData(data.packetName);
                newData.totalPacketBytes = data.totalPacketBytes;
                newData.maxPacketBytes = data.maxPacketBytes;
                newData.packetCount = data.packetCount;
                dataMap.put(data.packetName, newData);
            }
        }
    }

    private static class PlayerPacketDataEntry
    {
        final List<PacketData>   packetData;
        final long               totalBytes;
        final double             totalRate;
        final ServerPlayerEntity playerEntity;

        private PlayerPacketDataEntry(
          final List<PacketData> packetData,
          final long totalBytes,
          final double totalRate,
          final ServerPlayerEntity playerEntity)
        {
            this.packetData = packetData;
            this.totalBytes = totalBytes;
            this.totalRate = totalRate;
            this.playerEntity = playerEntity;
        }
    }

    /**
     * Sends a summary report to the source
     *
     * @param source
     */
    public static void reportAllPlayerSummary(final CommandSource source, final int minutes, final int startIndex)
    {
        DecimalFormat percent = new DecimalFormat("########.##");
        final Style GREEN_BOLD = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.GREEN));
        final Style BLUE = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.BLUE));
        final Style RED = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.RED));

        final List<PlayerPacketDataEntry> playerData = new ArrayList<>();
        for (final ServerPlayerEntity player : source.getLevel().getServer().getPlayerList().getPlayers())
        {
            playerData.add(getDataForPlayer(player, minutes));
        }
        playerData.sort(Comparator.<PlayerPacketDataEntry>comparingLong(data -> data.totalBytes).reversed());

        source.sendSuccess(new StringTextComponent("Network players summary of last " + minutes + " minutes."), false);

        long byteSum = 0;
        for (final PlayerPacketDataEntry packetData : playerData)
        {
            byteSum += packetData.totalBytes;
        }

        int i = 0;
        for (i = startIndex; i < startIndex + 5 && i < playerData.size(); i++)
        {
            final PlayerPacketDataEntry playerPacketDataEntry = playerData.get(i);
            source.sendSuccess(
              new StringTextComponent(percent.format(((double) (playerPacketDataEntry.totalBytes) / (byteSum)) * 100) + "% ")
                .append(new StringTextComponent(playerPacketDataEntry.playerEntity.getName().getString() + " ")).setStyle(GREEN_BOLD)
                .append(new StringTextComponent("r: " + percent.format(playerPacketDataEntry.totalRate) + "kb/s ").setStyle(BLUE))
                .append(new StringTextComponent("total sent: " + percent.format((double) playerPacketDataEntry.totalBytes / 1000d) + "kb").setStyle(RED))
              , false);
        }

        if (i + 1 < playerData.size())
        {
            source.sendSuccess(new StringTextComponent("next --->").setStyle(Style.EMPTY.withBold(true)
                                                                               .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                                 String.format(NETWORKSTATS_PLAYER_SUMMARY_COMMAND, minutes, i)))), false);
        }
    }

    /**
     * Sends a summary report to the source
     *
     * @param source
     */
    public static void reportStatsSummary(final CommandSource source, final int minutes, final int startIndex)
    {
        List<PacketData> data = getDataByPacket(minutes);
        DecimalFormat percent = new DecimalFormat("########.##");
        final Style GREEN_BOLD = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.GREEN));
        final Style BLUE = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.BLUE));
        final Style YELLOW = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.YELLOW));
        final Style RED = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.RED));
        final Style GOLD = Style.EMPTY.withBold(true).withColor(Color.fromLegacyFormat(TextFormatting.GOLD));

        source.sendSuccess(new StringTextComponent("Network packet summary of last " + minutes + " minutes."), false);

        long totalBytes = 0;
        double totalRate = 0;
        for (final PacketData packetData : data)
        {
            totalBytes += packetData.totalPacketBytes;
            totalRate += packetData.rate;
        }
        source.sendSuccess(new StringTextComponent("Total kb:" + percent.format(totalBytes / 1000d) + " total rate:" + percent.format(totalRate)).setStyle(GOLD), false);

        int i = 0;
        for (i = startIndex; i < startIndex + 5 && i < data.size(); i++)
        {
            final PacketData packetData = data.get(i);
            source.sendSuccess(
              new StringTextComponent(percent.format(((double) (packetData.totalPacketBytes) / (totalBytes)) * 100) + "% ")
                .append(new StringTextComponent(packetData.packetName + " ")).setStyle(GREEN_BOLD)
                .append(new StringTextComponent("r: " + percent.format(packetData.rate) + "kb/s ").setStyle(BLUE))
                .append(new StringTextComponent("count:" + packetData.packetCount + " ").setStyle(YELLOW))
                .append(new StringTextComponent("maxSize: " + percent.format((double) packetData.maxPacketBytes / 1000d) + "kb").setStyle(RED))
              , false);
        }

        if (i + 1 < data.size())
        {
            source.sendSuccess(new StringTextComponent("next --->").setStyle(Style.EMPTY.withBold(true)
                                                                               .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                                 String.format(NETWORKSTATS_SUMMARY_COMMAND, minutes, i)))), false);
        }
    }

    /**
     * Sends a summary report to the source
     *
     * @param source
     */
    public static void reportPlayerSummary(final CommandSource source, final ServerPlayerEntity playerEntity, final int minutes, final int startIndex)
    {
        if (playerEntity == null)
        {
            source.sendSuccess(new StringTextComponent("Player not found"), true);
            return;
        }

        final PlayerPacketDataEntry playerPacketData = getDataForPlayer(playerEntity, minutes);
        final List<PacketData> data = playerPacketData.packetData;
        DecimalFormat percent = new DecimalFormat("########.##");
        final Style GREEN_BOLD = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.GREEN));
        final Style BLUE = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.BLUE));
        final Style YELLOW = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.YELLOW));
        final Style RED = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.RED));
        final Style GOLD = Style.EMPTY.withBold(true).withColor(Color.fromLegacyFormat(TextFormatting.GOLD));

        source.sendSuccess(new StringTextComponent("Network packet summary for " + playerEntity.getName().getString() + " of last " + minutes + " minutes."), false);

        long totalBytes = 0;
        double totalRate = 0;
        for (final PacketData packetData : data)
        {
            totalBytes += packetData.totalPacketBytes;
            totalRate += packetData.rate;
        }
        source.sendSuccess(new StringTextComponent("Total kb:" + percent.format(totalBytes / 1000d) + " total rate:" + percent.format(totalRate)).setStyle(GOLD), false);

        int i = 0;
        for (i = startIndex; i < startIndex + 5 && i < data.size(); i++)
        {
            final PacketData packetData = data.get(i);
            source.sendSuccess(
              new StringTextComponent(percent.format(((double) (packetData.totalPacketBytes) / (totalBytes)) * 100) + "% ")
                .append(new StringTextComponent(packetData.packetName + " ")).setStyle(GREEN_BOLD)
                .append(new StringTextComponent("r: " + percent.format(packetData.rate) + "kb/s ").setStyle(BLUE))
                .append(new StringTextComponent("count:" + packetData.packetCount + " ").setStyle(YELLOW))
                .append(new StringTextComponent("maxSize: " + percent.format((double) packetData.maxPacketBytes / 1000d) + "kb").setStyle(RED))
              , false);
        }

        if (i + 1 < data.size())
        {
            source.sendSuccess(new StringTextComponent("next --->").setStyle(Style.EMPTY.withBold(true)
                                                                               .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                                 String.format(NETWORKSTATS_SINGLE_PLAYER_COMMAND,
                                                                                   playerEntity.getName().getString(),
                                                                                   minutes,
                                                                                   i)))), false);
        }
    }

    /**
     * Outgoing summary for client
     *
     * @param source
     */
    public static void reportClientStatsSummary(final PlayerEntity playerEntity, final int minutes, final int startIndex)
    {
        if (playerEntity == null)
        {
            return;
        }

        List<PacketData> data = getDataByPacket(minutes);
        DecimalFormat percent = new DecimalFormat("########.##");
        final Style GREEN_BOLD = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.GREEN));
        final Style BLUE = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.BLUE));
        final Style YELLOW = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.YELLOW));
        final Style RED = Style.EMPTY.withBold(false).withColor(Color.fromLegacyFormat(TextFormatting.RED));
        final Style GOLD = Style.EMPTY.withBold(true).withColor(Color.fromLegacyFormat(TextFormatting.GOLD));

        playerEntity.sendMessage(new StringTextComponent("Network outgoing packet summary of last " + minutes + " minutes."), playerEntity.getUUID());

        long totalBytes = 0;
        double totalRate = 0;
        for (final PacketData packetData : data)
        {
            totalBytes += packetData.totalPacketBytes;
            totalRate += packetData.rate;
        }
        playerEntity.sendMessage(new StringTextComponent("Total kb:" + percent.format(totalBytes / 1000d) + " total rate:" + percent.format(totalRate)).setStyle(GOLD),
          playerEntity.getUUID());

        int i = 0;
        for (i = startIndex; i < startIndex + 5 && i < data.size(); i++)
        {
            final PacketData packetData = data.get(i);
            playerEntity.sendMessage(
              new StringTextComponent(percent.format(((double) (packetData.totalPacketBytes) / (totalBytes)) * 100) + "% ")
                .append(new StringTextComponent(packetData.packetName + " ")).setStyle(GREEN_BOLD)
                .append(new StringTextComponent("r: " + percent.format(packetData.rate) + "kb/s ").setStyle(BLUE))
                .append(new StringTextComponent("count:" + packetData.packetCount + " ").setStyle(YELLOW))
                .append(new StringTextComponent("maxSize: " + percent.format((double) packetData.maxPacketBytes / 1000d) + "kb").setStyle(RED))
              , playerEntity.getUUID());
        }

        if (i + 1 < data.size())
        {
            playerEntity.sendMessage(new StringTextComponent("next --->").setStyle(Style.EMPTY.withBold(true)
                                                                                     .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                                       String.format(NETWORKSTATS_CLIENT_FAKE_COMMAND, minutes, i)))), playerEntity.getUUID());
        }
    }
}
