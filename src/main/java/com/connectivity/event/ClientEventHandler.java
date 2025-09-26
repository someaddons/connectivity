package com.connectivity.event;

import com.connectivity.Connectivity;
import com.connectivity.networkstats.NetworkStatGatherer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Client side event handler, used to fake a client command
 */
public class ClientEventHandler
{
    public static void on(final String command)
    {
        if (command.contains("connectivity packetsClient"))
        {
            String[] split = command.split(" ");
            int minutes = 5;
            int index = 0;


            if (split.length > 2)
            {
                try
                {
                    minutes = Math.min(Integer.parseInt(split[2]), Connectivity.config.getCommonConfig().packetHistoryMinutes);
                }
                catch (Exception e)
                {
                    Minecraft.getInstance().player.displayClientMessage(Component.literal("Excepted number of minutes as first parameter"), false);
                    return;
                }

                if (split.length > 3)
                {
                    try
                    {
                        index = Integer.parseInt(split[2]);
                    }
                    catch (Exception e)
                    {
                        Minecraft.getInstance().player.displayClientMessage(Component.literal("Excepted number of shown row as second parameter"), false);
                        return;
                    }
                }
            }

            NetworkStatGatherer.reportClientStatsSummary(Minecraft.getInstance().player, minutes, index);
        }
    }

    static long lastTime = 0;
    static int  counter  = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        if (counter++ > 100)
        {
            counter = 0;
            if (System.currentTimeMillis() - lastTime >= 60000)
            {
                lastTime = System.currentTimeMillis();
                NetworkStatGatherer.saveData();
            }
        }
    }
}
