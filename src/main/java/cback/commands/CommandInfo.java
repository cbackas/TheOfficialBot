package cback.commands;

import cback.OfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.time.ZoneOffset;
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
        return Arrays.asList("serverinfo", "server", "stats", "about");
    }

    @Override
    public String getSyntax() {
        return "info";
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
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
        int userCount = guild.getUsers().size();
        int oldUserCount = Integer.valueOf(bot.getConfigManager().getConfigValue("userCount"));

        int newCount = userCount - oldUserCount;
        String leaveJoin = " (-" + bot.getConfigManager().getConfigValue("left") + " +" + bot.getConfigManager().getConfigValue("joined") + ")";
        String userChange = newCount + leaveJoin;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

        EmbedBuilder embed = Util.getEmbed(message.getAuthor()).withThumbnail(Util.getAvatar(client.getOurUser()));

        embed.withTitle(guild.getName())
                .appendField("Created: ", guild.getCreationDate().atOffset(ZoneOffset.ofHours(0)).format(formatter), true)
                .appendField("\u200B", "\u200B", false)
                .appendField("Users: ", Integer.toString(userCount), true)
                .appendField("New Users: ", userChange, true)
                .appendField("Text Channels: ", String.valueOf(client.getChannels(false).size()), true)
                .appendField("Bot Uptime: ", OfficialBot.getInstance().getUptime(), true)
                .appendField("\u200B", "\u200B", false)
                .appendField("Listen:", "[`iTunes`](https://itunes.apple.com/au/podcast/the-official-podcast/id1186089636)" +
                "\n[`SoundCloud`](https://soundcloud.com/theofficialpodcast)" +
                "\n[`Spotify`](https://open.spotify.com/show/6TXzjtMTEopiGjIsCfvv6W)", true)
                .appendField("Other Links: ", "[`The Official Website`](http://www.theofficialpodcast.com)\n[`Reddit`](https://www.reddit.com/r/TheOfficialPodcast/)", true)
                .appendField("\u200B", "\u200B", false)
                .appendField("Donate to bot hosting fees: ", "[`Paypal`](https://www.paypal.me/cbackas)", true)
                .appendField("Source: ", "[`GitHub`](https://github.com/cbackas/OfficialBot)", true)
                .withColor(OfficialBot.getBotColor());

        Util.sendEmbed(message.getChannel(), embed.build());
        Util.deleteMessage(message);
    }

}
