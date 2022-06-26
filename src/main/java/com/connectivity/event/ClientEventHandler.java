package com.connectivity.event;

import com.connectivity.Connectivity;
import com.connectivity.networkstats.NetworkStatGatherer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client side event handler, used to fake a client command
 */
public class ClientEventHandler
{
    public static void on(String message, final CallbackInfo ci)
    {
        if (message != null && message.contains("/connectivity packetsClient"))
        {
            ci.cancel();
            String[] split = message.split(" ");
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
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("Excepted number of minutes as first parameter"));
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
                        Minecraft.getInstance().player.sendSystemMessage(Component.literal("Excepted number of shown row as second parameter"));
                        return;
                    }
                }
            }

            NetworkStatGatherer.reportClientStatsSummary(Minecraft.getInstance().player, minutes, index);
        }
    }

    static long lastTime = 0;
    static int  counter  = 0;

    public static void onClientTick(final Minecraft minecraft)
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
