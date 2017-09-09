package cback.events;

import cback.OfficialRoles;
import cback.TheOfficialBot;
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
import java.util.List;

public class MemberChange {
    private TheOfficialBot bot;

    private final long MUTED_ROLE_ID = 269638591112544267l;
    private final long MEMBERLOG_CH_ID = 266655441449254914l;
    private final long STAFF_CH_ID = 266651712826114048l;
    private final long ADMIN_CH_ID = 285470408709373954l;

    public MemberChange(TheOfficialBot bot) {
        this.bot = bot;
    }

    @EventSubscriber
    public void memberJoin(UserJoinEvent event) {
        if (event.getGuild().getStringID().equals(TheOfficialBot.getHomeGuild().getStringID())) {
            IUser user = event.getUser();
            IGuild guild = TheOfficialBot.getHomeGuild();
            //Mute Check
            if (bot.getConfigManager().getConfigArray("muted").contains(user.getStringID())) {
                try {
                    user.addRole(guild.getRoleByID(MUTED_ROLE_ID));
                } catch (Exception e) {
                    Util.reportHome(e);
                }
            }

            //Join Counter
            int joinedUsers = Integer.parseInt(bot.getConfigManager().getConfigValue("joined"));
            bot.getConfigManager().setConfigValue("joined", String.valueOf(joinedUsers + 1));

            //Notifier
            int totalUsers = guild.getUsers().size();
            if (totalUsers % 1000 == 0) {
                Util.sendMessage(guild.getChannelByID(ADMIN_CH_ID), guild.getRoleByID(OfficialRoles.ADMIN.id).mention() + " we have hit " + totalUsers + " users hype");
            }

            //Member-log
            EmbedBuilder bld = new EmbedBuilder()
                    .withDesc(Util.getTag(user) + " **joined** the server. " + user.mention());

            long userCreated = user.getCreationDate().toEpochSecond(ZoneOffset.ofHours(0));
            long currentTime = Util.getCurrentTime();
            if (currentTime - userCreated < 86400) {
                bld.withFooterText("**NEW ACCOUNT**");
            }

            bld
                    .withTimestamp(System.currentTimeMillis())
                    .withColor(Color.CYAN);

            Util.sendEmbed(guild.getChannelByID(MEMBERLOG_CH_ID), bld.build());
        }
    }

    @EventSubscriber
    public void memberLeave(UserLeaveEvent event) {
        if (event.getGuild().getStringID().equals(TheOfficialBot.getHomeGuild().getStringID())) {
            IUser user = event.getUser();
            IGuild guild = TheOfficialBot.getHomeGuild();

            //Mute Check
            if (bot.getConfigManager().getConfigArray("muted").contains(event.getUser().getStringID())) {
                Util.sendMessage(guild.getChannelByID(STAFF_CH_ID), user + " is muted and left the server. Their mute will be applied again when/if they return.");
            }

            //Leave Counter
            int left = Integer.parseInt(bot.getConfigManager().getConfigValue("left"));
            bot.getConfigManager().setConfigValue("left", String.valueOf(left + 1));

            //Member-log
            EmbedBuilder bld = new EmbedBuilder()
                    .withDesc(Util.getTag(user) + " **left** the server. " + user.mention())
                    .withTimestamp(System.currentTimeMillis())
                    .withColor(Color.YELLOW);

            Util.sendEmbed(guild.getChannelByID(MEMBERLOG_CH_ID), bld.build());
        }
    }

    @EventSubscriber
    public void memberBanned(UserBanEvent event) {
        if (event.getGuild().getStringID().equals(TheOfficialBot.getHomeGuild().getStringID())) {
            IUser user = event.getUser();
            IGuild guild = TheOfficialBot.getHomeGuild();

            //Mute Check
            if (bot.getConfigManager().getConfigArray("muted").contains(user.getStringID())) {
                List<String> mutedUsers = bot.getConfigManager().getConfigArray("muted");
                mutedUsers.remove(user.getStringID());
                bot.getConfigManager().setConfigValue("muted", mutedUsers);
            }

            //Leave Counter
            int left = Integer.parseInt(bot.getConfigManager().getConfigValue("left"));
            bot.getConfigManager().setConfigValue("left", String.valueOf(left + 1));

            //Member-log
            EmbedBuilder bld = new EmbedBuilder()
                    .withDesc(Util.getTag(user) + " was **banned** from the server. " + user.mention())
                    .withTimestamp(System.currentTimeMillis())
                    .withColor(Color.RED);

            Util.sendEmbed(guild.getChannelByID(MEMBERLOG_CH_ID), bld.build());
        }
    }

    @EventSubscriber
    public void memberUnbanned(UserPardonEvent event) {
        if (event.getGuild().getStringID().equals(TheOfficialBot.getHomeGuild().getStringID())) {
            IUser user = event.getUser();
            IGuild guild = event.getGuild();

            //Member-log
            EmbedBuilder bld = new EmbedBuilder()
                    .withDesc(Util.getTag(user) + " was **unbanned** from the server. " + user.mention())
                    .withTimestamp(System.currentTimeMillis())
                    .withColor(Color.GREEN);

            Util.sendEmbed(guild.getChannelByID(MEMBERLOG_CH_ID), bld.build());
        }
    }
}
