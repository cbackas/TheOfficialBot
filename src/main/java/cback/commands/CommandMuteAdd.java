package cback.commands;

import cback.OfficialRoles;
import cback.OfficialBot;
import cback.ServerLog;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandMuteAdd implements Command {
    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "mute @user [reason?]";
    }

    @Override
    public String getDescription() {
        return "Mutes a user and logs the action";
    }

    @Override
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.STAFF.id);
    }

    OfficialBot bot = OfficialBot.getInstance();

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("list")) {
                muteList(message.getChannel());
            } else if (args[0].matches("<@!?(\\d)+>")) {
                muteUser(message);
            } else {
                Util.syntaxError(this, message);
            }
        }
        Util.deleteMessage(message);
    }

    private void muteList(IChannel channel) {
        List<String> mutedUsers = bot.getConfigManager().getConfigArray("muted");
        StringBuilder mutedList = new StringBuilder();
        if (!mutedUsers.isEmpty()) {
            for (String userID : mutedUsers) {
                IUser userO = OfficialBot.getClient().getUserByID(Long.parseLong(userID));

                String user = "<@" + userID + ">";
                if (userO != null) {
                    user = userO.mention();
                }

                mutedList.append("\n").append(user);
            }
        } else {
            mutedList.append("\n").append("There are currently no muted users.");
        }

        Util.simpleEmbed(channel, "Muted Users: (plain text for users not on server)\n" + mutedList.toString());
    }

    private void muteUser(IMessage message) {
        IGuild guild = message.getGuild();
        String stuff = message.getContent().split(" ", 2)[1];

        Pattern pattern = Pattern.compile("^<@!?(\\d+)> ?(.+)?");
        Matcher matcher = pattern.matcher(stuff);
        if (matcher.find()) {
            String u = matcher.group(1);
            String reason = matcher.group(2);

            IUser user = guild.getUserByID(Long.parseLong(u));
            if (user != null) {
                if (reason == null) {
                    reason = "an unspecified reason";
                }

                if (message.getAuthor().getStringID().equals(u)) {
                    Util.simpleEmbed(message.getChannel(), "You probably shouldn't mute yourself");
                    return;
                }
            }

            try {
                List<String> mutedUsers = bot.getConfigManager().getConfigArray("muted");

                user.addRole(guild.getRoleByID(Long.parseLong("281022564002824192")));
                Util.simpleEmbed(message.getChannel(), user.getDisplayName(guild) + " has been muted. Check " + guild.getChannelByID(OfficialBot.SERVERLOG_CH_ID).mention() + " for more info.");

                if (!mutedUsers.contains(user.getStringID())) {
                    mutedUsers.add(user.getStringID());
                    bot.getConfigManager().setConfigValue("muted", mutedUsers);
                }

                new ServerLog(message.getAuthor(), "Muted " + Util.getTag(user) + " (" + user.getStringID() + ")" + "\n**Reason:** " + reason).send();
            } catch (Exception e) {
                Util.simpleEmbed(message.getChannel(), "Error running " + this.getName() + " - error recorded");
                Util.reportHome(message, e);
            }
        }
    }
}
