package cback;

import sx.blah.discord.handle.obj.IGuild;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    /**
     * Check for airings (and delete messages from old airings) every X seconds
     */
    private static final int CHECK_AIRING_INTERVAL = 300; //5 minutes
    /**
     * Number of seconds in one day
     */
    private static final int DAILY_INTERVAL = 86400; //24 hours
    /**
     * Send alert if show airs within X seconds from time of checking
     */
    private static final int ALERT_TIME_THRESHOLD = 660; //11 minutes
    /**
     * Delete message from announcements channel if show aired over X seconds from time of checking
     */
    private static final int DELETE_THRESHOLD = 7000; //~2 hours

    private TheOfficialBot bot;

    public Scheduler(TheOfficialBot bot) {
        this.bot = bot;
        onInit();
    }

    private void onInit() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

        int time = Util.getCurrentTime(); //current epoch time in seconds

        //update user count at midnight every night
        int currentTimeEST = time - 14400; //EST time, subtract 4 hours from UTC
        int midnightWaitTime = roundUp(currentTimeEST, DAILY_INTERVAL) - currentTimeEST; //seconds until midnight
        exec.scheduleAtFixedRate(() -> {

            updateUserCount();
            resetUserChange();

        }, midnightWaitTime, DAILY_INTERVAL, TimeUnit.SECONDS);
    }


    /**
     * Update the number of Lounge server members in the config
     */
    public void updateUserCount() {
        IGuild loungeGuild = bot.getClient().getGuildByID("266649217538195457");
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