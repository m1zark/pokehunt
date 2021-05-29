package com.m1zark.pokehunt.tasks;

import com.m1zark.m1utilities.M1utilities;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Discord.Message;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.pokehunt.PokeHunt;
import com.m1zark.pokehunt.util.BountiesManager;
import com.m1zark.pokehunt.util.HuntsManager;
import com.m1zark.pokehunt.config.BountiesConfig;
import com.m1zark.pokehunt.config.Config;
import com.m1zark.pokehunt.config.HuntsConfig;
import com.m1zark.pokehunt.config.MessageConfig;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

public class checkExpired {
    private static Task task;

    public static void initialize() {
        PokeHunt.getInstance().getSql().initializeBounties();
        PokeHunt.getInstance().getSql().initializeHunts();

        task = Task.builder().execute(t -> {
            int i = 1;
            boolean msg = false;
            while(i <= BountiesConfig.getNumberBounties()) {
                Time time = new Time(PokeHunt.getInstance().getSql().getExpires(i,"bounties").toInstant().toEpochMilli());
                if (time.toString("").equalsIgnoreCase("Expired")) {
                    PokeHunt.getInstance().getSql().updateBounties(new BountiesManager(i), true);
                    if(!msg) {
                        Chat.sendServerWideMessage(MessageConfig.getMessages("Messages.Misc.announceNewHunts").replace("{type}","Bounties"));
                        msg = true;
                    }
                }
                i++;
            }

            i = 1;
            msg = false;

            while(i <= HuntsConfig.getNumberHunts()) {
                Time time = new Time(PokeHunt.getInstance().getSql().getExpires(i,"hunts").toInstant().toEpochMilli());
                if (time.toString("").equalsIgnoreCase("Expired")) {
                    PokeHunt.getInstance().getSql().updateHunts(new HuntsManager(i), true);
                    if(!msg) {
                        Chat.sendServerWideMessage(MessageConfig.getMessages("Messages.Misc.announceNewHunts").replace("{type}","Hunts"));
                        msg = true;
                    }
                }
                i++;
            }
        }).interval(10, TimeUnit.MINUTES).delay(10, TimeUnit.MINUTES).async().name("PokeHunts").submit(PokeHunt.getInstance());
    }
}
