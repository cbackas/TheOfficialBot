package cback.commands;

import cback.OfficialRoles;
import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.PermissionUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandKick implements Command {
    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "?kick @user [reason]";
    }

    @Override
    public String getDescription() {
        return "Kicks a user from the server";
    }

    @Override
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.MOD.id, OfficialRoles.ADMIN.id);
    }

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, TheOfficialBot bot) {
        String text = message.getContent();
        IUser mod = message.getAuthor();
        try {
            PermissionUtils.hasPermissions(message.getChannel(), message.getAuthor(), EnumSet.of(Permissions.KICK));
            Pattern pattern = Pattern.compile("^\\?kick <@!?(\\d+)> ?(.+)?");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String userInput = matcher.group(1);
                String reason = matcher.group(2);
                if (reason != null) {
                    IUser user = guild.getUserByID(Long.parseLong(userInput));
                    if (user.getStringID().equals(mod.getStringID())) {
                        Util.sendMessage(message.getChannel(), "You know you can just leave right?");
                    } else {
                        try {
                            guild.kickUser(user);
                            Util.sendLog(message, "Kicked " + user.getDisplayName(guild) + "\n**Reason:** " + reason, Color.gray);
                            Util.sendMessage(message.getChannel(), user.getDisplayName(guild) + " has been kicked. Check " + guild.getChannelByID(Long.parseLong(TheOfficialBot.LOG_CHANNEL_ID)).mention() + " for more info");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Util.sendMessage(message.getChannel(), "Internal error - cback has been notified");
                            Util.sendPrivateMessage(client.getUserByID(Long.parseLong("73416411443113984")), "Error running CommandKick - check stacktrace");
                        }
                    }
                } else {
                    Util.sendPrivateMessage(mod, "**Error Kicking**: Reason required");
                }
            } else {
                Util.sendMessage(message.getChannel(), "Invalid arguments. Usage: ``?kick @user reason``");
            }
        } catch (Exception e) {
        }

        Util.deleteMessage(message);
    }

}
