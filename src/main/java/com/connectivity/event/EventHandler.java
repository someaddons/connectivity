package com.connectivity.event;

import com.connectivity.networkstats.NetworkStatGatherer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class EventHandler
{
    static long lastTime = 0;
    static int  counter  = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event)
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
