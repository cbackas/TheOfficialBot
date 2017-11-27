package cback.commands;

import cback.OfficialBot;
import cback.OfficialRoles;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.List;

public class CommandNextCallerClear implements Command {
    @Override
    public String getName() {
        return "clearcallers";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("cc");
    }

    @Override
    public String getSyntax() {
        return "clearcallers";
    }

    @Override
    public String getDescription() {
        return "Clears the callers";
    }

    @Override
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.ADMIN.id);
    }

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
        OfficialBot.alreadyCalled.clear();
        Util.simpleEmbed(message.getChannel(), "Called users cleared.");
    }
}
