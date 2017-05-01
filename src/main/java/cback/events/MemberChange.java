package cback.events;

import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.member.UserBanEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.time.ZoneOffset;
import java.util.List;

public class MemberChange {
    private TheOfficialBot bot;

    public MemberChange(TheOfficialBot bot) {
        this.bot = bot;
    }

    @EventSubscriber
    public void memberJoin(UserJoinEvent event) {
        IUser user = event.getUser();
        IGuild guild = event.getGuild();
        //Mute Check
       if (bot.getConfigManager().getConfigArray("muted").contains(user.getStringID())) {
            try {
                user.addRole(guild.getRoleByID(Long.parseLong("269638591112544267")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Join Counter
        int joined = Integer.parseInt(bot.getConfigManager().getConfigValue("joined"));
        bot.getConfigManager().setConfigValue("joined", String.valueOf(joined + 1));

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
                .withColor(Color.GREEN);

        Util.sendEmbed(guild.getChannelByID(266655441449254914l), bld.build());
    }

    @EventSubscriber
    public void memberLeave(UserLeaveEvent event) {
        IUser user = event.getUser();
        IGuild guild = event.getGuild();

        //Mute Check
        if (bot.getConfigManager().getConfigArray("muted").contains(event.getUser().getStringID())) {
            Util.sendMessage(guild.getChannelByID(Long.parseLong("266651712826114048")), user + " is muted and left the server. Their mute will be applied again when/if they return.");
        }

        //Leave Counter
        int left = Integer.parseInt(bot.getConfigManager().getConfigValue("left"));
        bot.getConfigManager().setConfigValue("left", String.valueOf(left + 1));

        //Member-log
        EmbedBuilder bld = new EmbedBuilder()
                .withDesc(Util.getTag(user) + " **left** the server. " + user.mention())
                .withTimestamp(System.currentTimeMillis())
                .withColor(Color.YELLOW);

        Util.sendEmbed(guild.getChannelByID(266655441449254914l), bld.build());

    }

    @EventSubscriber
    public void memberBanned(UserBanEvent event) {
        IUser user = event.getUser();
        IGuild guild = event.getGuild();

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
                .withDesc(Util.getTag(user) + " was **banned** the server. " + user.mention())
                .withTimestamp(System.currentTimeMillis())
                .withColor(Color.RED);

        Util.sendEmbed(guild.getChannelByID(266655441449254914l), bld.build());
    }
}
