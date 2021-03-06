package cback.commands;

import cback.OfficialBot;
import cback.OfficialRoles;
import cback.ServerLog;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.List;

public class CommandAddLog implements Command {
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
        return "addlog [log message]";
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
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
        if (args.length >= 1) {
            String finalText = message.getFormattedContent().split(" ", 2)[1];
            new ServerLog(author, finalText).send();

            if (message.getChannel().getLongID() != OfficialBot.SERVERLOG_CH_ID) {
                Util.simpleEmbed(message.getChannel(), "Log added. " + guild.getChannelByID(OfficialBot.SERVERLOG_CH_ID).mention());
            }

            Util.deleteMessage(message);
        } else {
            Util.syntaxError(this, message);
        }
    }

}
