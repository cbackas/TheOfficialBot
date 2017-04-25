package cback.commands;

import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.util.List;

public class CommandTrigger implements Command {
    @Override
    public String getName() {
        return "trigger";
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
    public void execute(TheOfficialBot bot, IDiscordClient client, String[] args, IGuild guild, List<Long> roleIDs, IMessage message, boolean isPrivate) {
        if (message.getAuthor().getStringID().equals("73416411443113984")) {
            String fullRule = TheOfficialBot.getInstance().getClient().getChannelByID(Long.parseLong("251916332747063296")).getMessageByID(Long.parseLong("251922232069193728")).getContent();
            System.out.println(fullRule);


            Util.deleteMessage(message);
        }
    }

}
