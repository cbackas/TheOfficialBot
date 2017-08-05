package cback.commands;

import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class CommandInfo implements Command {
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("serverinfo", "server", "stats");
    }

    @Override
    public String getSyntax() {
        return "?info";
    }

    @Override
    public String getDescription() {
        return "Displays some statistics about the server and the bot";
    }

    @Override
    public List<Long> getPermissions() {
        return null;
    }

    @Override
    public void execute(TheOfficialBot bot, IDiscordClient client, String[] args, IGuild guild, List<Long> roleIDs, IMessage message, boolean isPrivate) {
        int userCount = guild.getUsers().size();
        int oldUserCount = Integer.valueOf(bot.getConfigManager().getConfigValue("userCount"));

        int newCount = userCount - oldUserCount;
        String leaveJoin = " (-" + bot.getConfigManager().getConfigValue("left") + " +" + bot.getConfigManager().getConfigValue("joined") + ")";
        String userChange = newCount + leaveJoin;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

        EmbedBuilder embed = Util.getEmbed(message.getAuthor()).withThumbnail(Util.getAvatar(client.getOurUser()));
        embed.withTitle(guild.getName());
        embed.appendField("Created: ", guild.getCreationDate().format(formatter), true);

        embed.appendField("\u200B", "\u200B", false);

        embed.appendField("Users: ", Integer.toString(userCount), true);
        embed.appendField("New Users: ", userChange, true);
        embed.appendField("Text Channels: ", String.valueOf(client.getChannels(false).size()), true);
        embed.appendField("Bot Uptime: ", TheOfficialBot.getInstance().getUptime(), true);

        embed.appendField("\u200B", "\u200B", false);

        embed.appendField("Listen:", "[`iTunes`](https://itunes.apple.com/au/podcast/the-official-podcast/id1186089636)" +
                "\n[`SoundCloud`](https://soundcloud.com/theofficialpodcast)" +
                "\n[`Spotify`](https://open.spotify.com/show/6TXzjtMTEopiGjIsCfvv6W)" +
                "\n[`The Website`](http://www.theofficialpodcast.com/episodes)", true);
        embed.appendField("Other Links: ", "[`The Official Website`](http://www.theofficialpodcast.com)\n[`Reddit`](https://www.reddit.com/r/TheOfficialPodcast/)", true);

        embed.appendField("\u200B", "\u200B", false);

        embed.appendField("Donate to bot hosting fees: ", "[`paypal.me`](paypal.me/cbackas)", true);
        embed.appendField("Source: ", "[`GitHub`](https://github.com/cbackas/TheOfficialBot)", true);

        Util.sendEmbed(message.getChannel(), embed.withColor(161, 61, 61).build());
        Util.deleteMessage(message);
    }

}
