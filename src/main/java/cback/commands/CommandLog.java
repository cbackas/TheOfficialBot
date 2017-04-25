package cback.commands;

import cback.OfficialRoles;
import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.util.Arrays;
import java.util.Collections;
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
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.STAFF.id);
    }

    @Override
    public void execute(TheOfficialBot bot, IDiscordClient client, String[] args, IGuild guild, List<Long> roleIDs, IMessage message, boolean isPrivate) {
        if (!Collections.disjoint(roleIDs, getPermissions())) {

            Util.botLog(message);

            if (args.length >= 1) {
                    String finalText = message.getFormattedContent().split(" ", 2)[1];
                    Util.sendLog(message, finalText);
                    Util.sendMessage(message.getChannel(), "Log added. " + guild.getChannelByID(Long.parseLong(TheOfficialBot.LOG_CHANNEL_ID)).mention());
                    Util.deleteMessage(message);
            } else {
                Util.sendMessage(message.getChannel(), "Usage: !addlog <text>");
            }
        }
    }

}
