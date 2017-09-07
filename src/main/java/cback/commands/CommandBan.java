package cback.commands;

import cback.OfficialRoles;
import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.PermissionUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandBan implements Command {
    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "ban @user [reason]";
    }

    @Override
    public String getDescription() {
        return "Bans a user from the server";
    }

    @Override
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.STAFF.id);
    }

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, TheOfficialBot bot) {
        String text = message.getContent();
        IUser mod = message.getAuthor();
        try {
            PermissionUtils.hasPermissions(message.getChannel(), message.getAuthor(), EnumSet.of(Permissions.BAN));
            Pattern pattern = Pattern.compile("^\\?ban <@!?(\\d+)> ?(.+)?");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String userInput = matcher.group(1);
                String reason = matcher.group(2);
                if (reason != null) {
                    IUser user = guild.getUserByID(Long.parseLong(userInput));
                    if (user.getStringID().equals(mod.getStringID())) {
                        Util.sendMessage(message.getChannel(), "You're gonna have to try harder than that.");
                    } else {
                        try {
                            guild.banUser(user, 1);
                            Util.sendLog(message, "Banned " + user.getDisplayName(guild) + "\n**Reason:** " + reason, Color.red);
                            Util.sendMessage(message.getChannel(), user.getDisplayName(guild) + " has been banned. Check " + guild.getChannelByID(Long.parseLong(TheOfficialBot.LOG_CHANNEL_ID)).mention() + " for more info.");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Util.sendMessage(message.getChannel(), "Internal error - cback has been notified");
                            Util.reportHome(message, e);
                        }
                    }
                } else {
                    Util.sendPrivateMessage(mod, "**Error Banning**: Reason required");
                }
            } else {
                Util.syntaxError(this, message);
            }
        } catch (Exception e) {
        }

        Util.deleteMessage(message);
    }

}
