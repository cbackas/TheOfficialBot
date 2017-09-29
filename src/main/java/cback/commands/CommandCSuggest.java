package cback.commands;

import cback.OfficialBot;
import cback.OfficialRoles;
import cback.Util;
import com.vdurmont.emoji.EmojiManager;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class CommandCSuggest implements Command {
    @Override
    public String getName() {
        return "csuggest";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("suggest");
    }

    @Override
    public String getSyntax() {
        return "csuggest [your fun idea here]";
    }

    @Override
    public String getDescription() {
        return "Puts your suggestion in the change log channel with some voting icons so people can vote about it. Discuss the topic in the staff channel.";
    }

    @Override
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.STAFF.id);
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
                    .withColor(Color.ORANGE);

            IMessage sentMessage = Util.sendEmbed(client.getChannelByID(OfficialBot.SERVERCHANGELOG_CH_ID), embed.build());

            RequestBuffer.request(() -> sentMessage.addReaction(EmojiManager.getByUnicode("\uD83D\uDCD7")));
            RequestBuffer.request(() -> sentMessage.addReaction(EmojiManager.getByUnicode("\uD83D\uDCD9")));

            if (message.getChannel().getLongID() != OfficialBot.SERVERCHANGELOG_CH_ID) {
                Util.simpleEmbed(message.getChannel(), "Suggestion added. " + guild.getChannelByID(OfficialBot.SERVERCHANGELOG_CH_ID).mention());
            }

            Util.deleteMessage(message);
        } else {
            Util.syntaxError(this, message);
        }
    }
}
