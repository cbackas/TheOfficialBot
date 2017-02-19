package cback.commands;

import cback.OfficialRoles;
import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;

import java.util.Arrays;
import java.util.List;

public class CommandLog implements Command {
    @Override
    public String getName() {
        return "addlog";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("log");
    }

    @Override
    public String getSyntax() {
        return "?addlog [log message]";
    }

    @Override
    public String getDescription() {
        return "Creates a new log in #logs with your desired message";
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList(OfficialRoles.STAFF.id);
    }

    @Override
    public void execute(TheOfficialBot bot, IDiscordClient client, String[] args, IGuild guild, IMessage message, boolean isPrivate) {
        if (message.getAuthor().getRolesForGuild(guild).contains(guild.getRoleByID(OfficialRoles.STAFF.id))) {

            Util.botLog(message);

            if (args.length >= 1) {
                List<IRole> userRoles = message.getAuthor().getRolesForGuild(guild);
                    String finalText = message.getFormattedContent().split(" ", 2)[1];
                    Util.sendLog(message, finalText);
                    Util.sendMessage(message.getChannel(), "Log added. " + guild.getChannelByID(TheOfficialBot.LOG_CHANNEL_ID).mention());
                    Util.deleteMessage(message);
            } else {
                Util.sendMessage(message.getChannel(), "Usage: !addlog <text>");
            }
        }
    }

}
