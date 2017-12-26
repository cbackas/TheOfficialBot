package cback.commands;

import cback.OfficialBot;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public class CommandSuggestAnon implements Command {
    @Override
    public String getName() {
        return "suggest";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "suggest [bla bla bla bla bla your suggestion here]";
    }

    @Override
    public String getDescription() {
        //return "Sends an ANONYMOUS suggestion to staff. cback *can* see who sent things if he went and looked tho so if you submit something super messed up you will probably get banned from the server.";
        return null;
    }

    @Override
    public List<Long> getPermissions() {
        return null;
    }

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
    }
}
