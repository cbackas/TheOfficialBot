package cback.commands;

import cback.OfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandReply implements Command {
    @Override
    public String getName() {
        return "reply";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getSyntax() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public List<Long> getPermissions() {
        return null;
    }

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
        String stuff = message.getContent().split(" ", 2)[1];

        Pattern pattern = Pattern.compile("^(\\d+) ?(.+)?");
        Matcher matcher = pattern.matcher(stuff);
        if (matcher.find()) {
            String user = matcher.group(1);
            String reply = matcher.group(2);

            if (reply != null) {
                Util.sendPrivateMessage(client.getUserByID(Long.parseLong(user)), reply);
            } else {
                Util.syntaxError(this, message);
            }
        } else {
            Util.syntaxError(this, message);
        }
    }
}
