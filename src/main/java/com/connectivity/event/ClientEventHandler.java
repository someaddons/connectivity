package com.connectivity.event;

import com.connectivity.Connectivity;
import com.connectivity.networkstats.NetworkStatGatherer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Client side event handler, used to fake a client command
 */
public class ClientEventHandler
{
    @SubscribeEvent
    public static void on(ClientChatEvent event)
    {
        if (event.getMessage().contains("/connectivity packetsClient"))
        {
            String[] split = event.getMessage().split(" ");
            int minutes = 5;
            int index = 0;

            if (split.length > 2)
            {
                try
                {
                    minutes = Math.min(Integer.parseInt(split[2]), Connectivity.config.getCommonConfig().packetHistoryMinutes.get());
                }
                catch (Exception e)
                {
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent("Excepted number of minutes as first parameter"), Minecraft.getInstance().player.getUUID());
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
                        Minecraft.getInstance().player.sendMessage(new StringTextComponent("Excepted number of shown row as second parameter"),
                          Minecraft.getInstance().player.getUUID());
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
    public static void onClientTick(TickEvent.ClientTickEvent event)
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
