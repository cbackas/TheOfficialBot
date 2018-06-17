package cback;

import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.StatusType;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    /**
     * Number of seconds in one day
     */
    private static final int DAILY_INTERVAL = 86400; //24 hours

    private OfficialBot bot;

    public Scheduler(OfficialBot bot) {
        this.bot = bot;
        onInit();
    }

    private void onInit() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

        int time = Util.getCurrentTime(); //current epoch time in seconds

        //update user count at midnight every night
        int currentTimeEST = time - getOffset(); //EST time, second offset changes depending on daylight savings
        int midnightWaitTime = roundUp(currentTimeEST, DAILY_INTERVAL) - currentTimeEST; //seconds until midnight
        exec.scheduleAtFixedRate(() -> {

            updateUserCount();
            resetUserChange();
            //Set status
            bot.getClient().changePresence(StatusType.ONLINE, ActivityType.WATCHING,"all of your messages. Type " + OfficialBot.prefix + "help");

        }, midnightWaitTime, DAILY_INTERVAL, TimeUnit.SECONDS);
    }

    private static int getOffset() {
        boolean inSavingsTime = TimeZone.getTimeZone( "US/Eastern").inDaylightTime( new Date() );
        if (inSavingsTime) {
            return 14400;
        } else {
            return 18000;
        }
    }

    /**
     * Update the number of Lounge server members in the config
     */
    public void updateUserCount() {
        IGuild loungeGuild = bot.getClient().getGuildByID(Long.parseLong("266649217538195457"));
        if (loungeGuild != null) {
            bot.getConfigManager().setConfigValue("userCount", String.valueOf(loungeGuild.getUsers().size()));
        }
    }

    /**
     * Reset daily user change
     */
    public void resetUserChange() {
        bot.getConfigManager().setConfigValue("left", "0");
        bot.getConfigManager().setConfigValue("joined", "0");
    }

    /**
     * Rounds i to next number divisible by v
     *
     * @param i
     * @param v
     * @return the rounded number divisible by v
     */
    public static int roundUp(double i, int v) {
        Double rounded = Math.ceil(i / v) * v;
        return rounded.intValue();
    }
}