package cback.commands;

import cback.OfficialBot;
import cback.OfficialRoles;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class CommandCLog implements Command {
    @Override
    public String getName() {
        return "clog";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "clog [stuff]";
    }

    @Override
    public String getDescription() {
        return "Adds a log to the changelog channel. This is a big deal.";
    }

    @Override
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.ADMIN.id);
    }

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
        if (args.length >= 1) {
            String finalText = message.getFormattedContent().split(" ", 2)[1];

            EmbedBuilder embed = new EmbedBuilder();

            embed
                    .withDesc(finalText)
                    .withFooterIcon(author.getAvatarURL())
                    .withFooterText(author.getDisplayName(guild))
                    .withTimestamp(System.currentTimeMillis())
                    .withColor(Color.GREEN);

            Util.sendEmbed(client.getChannelByID(OfficialBot.STAFFCHANGELOG_CH_ID), embed.build());

            if (message.getChannel().getLongID() != OfficialBot.STAFFCHANGELOG_CH_ID) {
                Util.simpleEmbed(message.getChannel(), "ChangeLog added. " + guild.getChannelByID(OfficialBot.STAFFCHANGELOG_CH_ID).mention());
            }

            Util.deleteMessage(message);
        } else {
            Util.syntaxError(this, message);
        }
    }
}
