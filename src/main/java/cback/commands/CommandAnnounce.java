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

public class CommandAnnounce implements Command {
    @Override
    public String getName() {
        return "announce";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "?announce [announcement text]";
    }

    @Override
    public String getDescription() {
        return "Sends a public announcement in both #announcements and #general";
    }

    @Override
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.ADMIN.id, OfficialRoles.HOST.id);
    }

    @Override
    public void execute(TheOfficialBot bot, IDiscordClient client, String[] args, IGuild guild, List<Long> roleIDs, IMessage message, boolean isPrivate) {
        if (!Collections.disjoint(roleIDs, getPermissions())) {
            String announcement = message.getContent().split(" ", 2)[1];
            Util.sendAnnouncement(announcement);

            Util.botLog(message);
            Util.deleteMessage(message);
        }
    }
}
