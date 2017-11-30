package cback.commands;

import cback.OfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public class CommandOptOut implements Command {
    @Override
    public String getName() {
        return "optout";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "optout";
    }

    @Override
    public String getDescription() {
        return "Removes you from the call pool for the episode 52 event.";
    }

    @Override
    public List<Long> getPermissions() {
        return null;
    }

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
        if (!OfficialBot.alreadyCalled.contains(author.getLongID())) {
            OfficialBot.alreadyCalled.add(author.getLongID());
            Util.sendMessage(message.getChannel(), author.mention() + " removed from the call pool.");
        }
        Util.deleteMessage(message);
    }
}
