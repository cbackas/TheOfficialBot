package cback.commands;

import cback.OfficialRoles;
import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandMuteRemove implements Command {
    @Override
    public String getName() {
        return "unmute";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "?unmute @user";
    }

    @Override
    public String getDescription() {
        return "Unmutes a user and sends a log message";
    }

    @Override
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.STAFF.id);
    }

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, TheOfficialBot bot) {
        if (args.length == 1) {
            String user = args[0];
            Pattern pattern = Pattern.compile("^<@!?(\\d+)>");
            Matcher matcher = pattern.matcher(user);
            if (matcher.find()) {

                String u = matcher.group(1);
                IUser userInput = guild.getUserByID(Long.parseLong(u));

                if (message.getAuthor().getStringID().equals(u)) {
                    Util.sendMessage(message.getChannel(), "Not sure how you typed this command... but you can't unmute yourself");
                } else {
                    try {
                        userInput.removeRole(guild.getRoleByID(Long.parseLong("281022564002824192")));

                        Util.sendMessage(message.getChannel(), userInput.getDisplayName(guild) + " has been unmuted");

                        List<String> mutedUsers = bot.getConfigManager().getConfigArray("muted");
                        if (mutedUsers.contains(u)) {
                            mutedUsers.remove(u);
                            bot.getConfigManager().setConfigValue("muted", mutedUsers);
                        }

                        Util.sendLog(message, userInput.getDisplayName(guild) + " has been unmuted.", Color.gray);
                        Util.deleteMessage(message);
                    } catch (Exception e) {
                    }
                }
            }
        } else {
            Util.sendMessage(message.getChannel(), "Invalid arguments. Usage: ``?unmute @user``");
        }
    }

}
