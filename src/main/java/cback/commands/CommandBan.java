package cback.commands;

import cback.OfficialBot;
import cback.OfficialRoles;
import cback.ServerLog;
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
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
        if (args.length >= 1) {
            try {
                PermissionUtils.hasPermissions(message.getChannel(), message.getAuthor(), EnumSet.of(Permissions.BAN));
                Pattern pattern = Pattern.compile("^\\?ban <@!?(\\d+)> ?(.+)?");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    String userInput = matcher.group(1);
                    String reason = matcher.group(2);
                    IUser user = guild.getUserByID(Long.parseLong(userInput));
                    if (reason != null && user != null) {
                        if (user.getStringID().equals(author.getStringID())) {
                            Util.simpleEmbed(message.getChannel(), "You're gonna have to try harder than that.");
                        } else {
                            try {
                                guild.banUser(user, reason, 1);

                                new ServerLog(author, "Banned " + Util.getTag(user) + " (" + user.getStringID() + ")" + "\n**Reason:** " + reason, Color.red).send();

                                Util.simpleEmbed(message.getChannel(), user.getDisplayName(guild) + " has been banned. Check " + guild.getChannelByID(OfficialBot.SERVERLOG_CH_ID).mention() + " for more info.");
                            } catch (Exception e) {
                                Util.simpleEmbed(message.getChannel(), "Error running " + this.getName() + " - error recorded");
                                Util.reportHome(message, e);
                            }
                        }
                    } else {
                        Util.sendPrivateMessage(author, "**Error Banning**: Reason required");
                    }
                } else {
                    Util.syntaxError(this, message);
                }
            } catch (Exception e) {
            }
            Util.deleteMessage(message);
        }
    }

}
