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
        return "Sends an ANONYMOUS suggestion to staff. cback *can* see who sent things if he went and looked tho so if you submit something super messed up you will probably get banned from the server.";
    }

    @Override
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.STAFF.id);
    }

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
        if (args.length >= 1) {
            String ideaText = message.getFormattedContent().split(" ", 2)[1];

            EmbedBuilder embed = new EmbedBuilder();

            embed
                    .withAuthorIcon("https://i.imgur.com/w2ufEiA.png")
                    .withAuthorName("Anon")
                    .withDesc(ideaText)
                    .withTimestamp(System.currentTimeMillis());

            if (Util.checkMessage(ideaText) != null) {
                embed.withColor(Color.red);
            } else {
                embed.withColor(Color.orange);
            }

            Util.sendEmbed(client.getChannelByID(OfficialBot.ANONSUGGESTIONS_CH_ID), embed.build());
            Util.sendPrivateEmbed(message.getAuthor(), "Your input has been anonymously forwarded to staff.");
            Util.deleteMessage(message);
        } else {
            Util.syntaxError(this, message);
        }
    }
}
