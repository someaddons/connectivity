package com.connectivity.event;

import com.connectivity.networkstats.NetworkStatGatherer;
import net.minecraft.server.MinecraftServer;

public class EventHandler
{
    static long lastTime = 0;
    static int  counter  = 0;

    public static void onServerTick(final MinecraftServer server)
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
