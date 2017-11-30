package cback.commands;

import cback.OfficialBot;
import cback.OfficialRoles;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandNextCaller implements Command {
    @Override
    public String getName() {
        return "nextcaller";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("nextup", "next");
    }

    @Override
    public String getSyntax() {
        return "nextcaller";
    }

    @Override
    public String getDescription() {
        return "Gives you a random user from the special channel so that they can be famous. Names don't get repeated.";
    }

    @Override
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.STAFF.id);
    }

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
        IVoiceChannel vc = client.getVoiceChannelByID(384832616467005440L);

        //bail if the channel search fails
        if (vc == null) return;

        List<Long> hosts = Arrays.asList(112839316685783040L, 196735363060858880L, 137294678721691648L, 185998642136481793L, OfficialBot.CBACK_USR_ID);

        List<IUser> channelUsers = vc.getConnectedUsers();
        List<IUser> callPool = channelUsers.stream()
                .filter(u -> !hosts.contains(u.getLongID()))
                .filter(u -> !OfficialBot.alreadyCalled.contains(u.getLongID()))
                .collect(Collectors.toList());

        if (callPool.size() == 0) {
            Util.sendMessage(client.getChannelByID(384832555024646145L), "There are no more unique personalities to talk to :(");
            return;
        }

        int min = 0;
        int max = callPool.size() - 1;
        int random = (min) + (int) (Math.random() * (max - min + 1));

        IUser nextCaller = callPool.get(random);
        if (nextCaller == null) {
            execute(message, content, args, author, guild, roleIDs, isPrivate, client, bot);
        }
        OfficialBot.alreadyCalled.add(nextCaller.getLongID());

        Util.sendMessage(client.getChannelByID(384832555024646145L), "Get ready " + nextCaller.mention() + "! You're about to be on the air!");
        Util.deleteMessage(message);
    }


}
