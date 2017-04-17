package cback.eventFunctions;

import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.member.UserBanEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public class MemberChange {
    private TheOfficialBot bot;

    public MemberChange(TheOfficialBot bot) {
        this.bot = bot;
    }

    @EventSubscriber
    public void memberJoin(UserJoinEvent event) {
        //Mute Check
       if (bot.getConfigManager().getConfigArray("muted").contains(event.getUser().getStringID())) {
            try {
                event.getUser().addRole(event.getGuild().getRoleByID("269638591112544267"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Join Counter
        int joined = Integer.parseInt(bot.getConfigManager().getConfigValue("joined"));
        bot.getConfigManager().setConfigValue("joined", String.valueOf(joined + 1));
    }

    @EventSubscriber
    public void memberLeave(UserLeaveEvent event) {
        IUser user = event.getUser();

        //Mute Check
        if (bot.getConfigManager().getConfigArray("muted").contains(event.getUser().getStringID())) {
            Util.sendMessage(event.getGuild().getChannelByID("266651712826114048"), user + " is muted and left the server. Their mute will be applied again when/if they return.");
        }

        //Leave Counter
        int left = Integer.parseInt(bot.getConfigManager().getConfigValue("left"));
        bot.getConfigManager().setConfigValue("left", String.valueOf(left + 1));


    }

    @EventSubscriber
    public void memberBanned(UserBanEvent event) {
        IUser user = event.getUser();

        //Mute Check
        if (bot.getConfigManager().getConfigArray("muted").contains(event.getUser().getStringID())) {
            List<String> mutedUsers = bot.getConfigManager().getConfigArray("muted");
            mutedUsers.remove(user.getStringID());
            bot.getConfigManager().setConfigValue("muted", mutedUsers);
        }

        //Leave Counter
        int left = Integer.parseInt(bot.getConfigManager().getConfigValue("left"));
        bot.getConfigManager().setConfigValue("left", String.valueOf(left + 1));
    }
}
