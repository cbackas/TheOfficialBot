package cback.events;

import cback.OfficialBot;
import cback.OfficialRoles;
import cback.Util;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.member.UserBanEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserPardonEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class MemberChange {
    private OfficialBot bot;

    private final long MUTED_ROLE_ID = 269638591112544267L;

    public MemberChange(OfficialBot bot) {
        this.bot = bot;
    }

    @EventSubscriber
    public void memberJoin(UserJoinEvent event) {
        if (event.getGuild().getStringID().equals(OfficialBot.getHomeGuild().getStringID())) {
            IUser user = event.getUser();
            String userID = user.getStringID();
            IGuild guild = OfficialBot.getHomeGuild();

            /**
             * Mute check
             */
            if (bot.getConfigManager().getConfigArray("muted").contains(user.getStringID())) {
                try {
                    user.addRole(guild.getRoleByID(MUTED_ROLE_ID));
                } catch (Exception e) {
                    Util.reportHome(e);
                }
            }

            /**
             * Member counter
             */
            int joinedUsers = Integer.parseInt(bot.getConfigManager().getConfigValue("joined"));
            bot.getConfigManager().setConfigValue("joined", String.valueOf(joinedUsers + 1));

            /**
             * Admin member-count notifier
             */
            int totalUsers = guild.getUsers().size();
            if (totalUsers % 1000 == 0) {
                Util.sendMessage(guild.getChannelByID(OfficialBot.ADMIN_CH_ID), "We have hit " + totalUsers + " users hype");
            }

            /**
             * Memberlog
             */
            EmbedBuilder bld = new EmbedBuilder()
                    .withDesc(Util.getTag(user) + " **joined** the server. <@" + userID + ">");

            //Checks if the new user's account is a new account (< 24 hours old)
            long userCreated = user.getCreationDate().atOffset(ZoneOffset.ofHours(0)).toEpochSecond();
            long currentTime = Util.getCurrentTime();
            if (currentTime - userCreated < 86400) {
                bld.withFooterText("**NEW ACCOUNT**");
            }

            bld
                    .withTimestamp(System.currentTimeMillis())
                    .withColor(Color.CYAN);

            Util.sendEmbed(guild.getChannelByID(OfficialBot.MEMBERLOG_CH_ID), bld.build());
        }
    }

    @EventSubscriber
    public void memberLeave(UserLeaveEvent event) {
        if (event.getGuild().getStringID().equals(OfficialBot.getHomeGuild().getStringID())) {
            IUser user = event.getUser();
            String userID = user.getStringID();
            IGuild guild = OfficialBot.getHomeGuild();

            /**
             * Mute check
             */
            if (bot.getConfigManager().getConfigArray("muted").contains(userID)) {
                Util.sendMessage(guild.getChannelByID(OfficialBot.STAFF_CH_ID), user + " is muted and left the server. Their mute will be applied again when/if they return.");
            }

            /**
             * Member counter
             */
            int left = Integer.parseInt(bot.getConfigManager().getConfigValue("left"));
            bot.getConfigManager().setConfigValue("left", String.valueOf(left + 1));

            /**
             * Memberlog
             */
            EmbedBuilder bld = new EmbedBuilder()
                    .withDesc(Util.getTag(user) + " **left** the server. <@" + userID + ">")
                    .withTimestamp(System.currentTimeMillis())
                    .withColor(Color.YELLOW);

            Util.sendEmbed(guild.getChannelByID(OfficialBot.MEMBERLOG_CH_ID), bld.build());
        }
    }

    @EventSubscriber
    public void memberBanned(UserBanEvent event) {
        if (event.getGuild().getStringID().equals(OfficialBot.getHomeGuild().getStringID())) {
            IUser user = event.getUser();
            IGuild guild = OfficialBot.getHomeGuild();

            /**
             * Mute check
             */
            if (bot.getConfigManager().getConfigArray("muted").contains(user.getStringID())) {
                List<String> mutedUsers = bot.getConfigManager().getConfigArray("muted");
                mutedUsers.remove(user.getStringID());
                bot.getConfigManager().setConfigValue("muted", mutedUsers);
            }

            /**
             * Member counter
             */
            int left = Integer.parseInt(bot.getConfigManager().getConfigValue("left"));
            bot.getConfigManager().setConfigValue("left", String.valueOf(left + 1));

            /**
             * Memberlog
             */
            EmbedBuilder bld = new EmbedBuilder()
                    .withDesc(Util.getTag(user) + " was **banned** from the server. " + user.mention())
                    .withTimestamp(System.currentTimeMillis())
                    .withColor(Color.RED);

            Util.sendEmbed(guild.getChannelByID(OfficialBot.MEMBERLOG_CH_ID), bld.build());
        }
    }

    @EventSubscriber
    public void memberPardoned(UserPardonEvent event) {
        if (event.getGuild().getStringID().equals(OfficialBot.getHomeGuild().getStringID())) {
            IUser user = event.getUser();
            IGuild guild = event.getGuild();

            /**
             * Memberlog
             */
            EmbedBuilder bld = new EmbedBuilder()
                    .withDesc(Util.getTag(user) + " was **unbanned** from the server. " + user.mention())
                    .withTimestamp(System.currentTimeMillis())
                    .withColor(Color.GREEN);

            Util.sendEmbed(guild.getChannelByID(OfficialBot.MEMBERLOG_CH_ID), bld.build());
        }
    }
}
