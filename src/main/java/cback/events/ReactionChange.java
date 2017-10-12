package cback.events;

import cback.OfficialBot;
import cback.OfficialRoles;
import cback.Util;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ReactionChange {
    private OfficialBot bot;

    public ReactionChange(OfficialBot bot) {
        this.bot = bot;
    }

    @EventSubscriber
    public void changeLogReactionAdd(ReactionAddEvent event) {
        if (event.getUser().isBot() || event.getChannel().getLongID() != OfficialBot.SERVERCHANGELOG_CH_ID) {
            return; //ignores bot reactions and reactions not related to change logs
        }
        IUser user = event.getUser();
        IMessage message = event.getMessage();
        IRole staff = event.getGuild().getRoleByID(OfficialRoles.STAFF.id);
        String emojiName = event.getReaction().getEmoji().getName();
        if (user.hasRole(staff) && (emojiName.equals("📗") || emojiName.equals("🔖") || emojiName.equals("📕"))) {
            if (hasReacted(message, user, event.getReaction()) || !hasReacted(message, OfficialBot.getClient().getOurUser(), event.getReaction())) {
                removeReaction(message, user, event.getReaction());
            } else {
                IEmbed sentEmbed = message.getEmbeds().get(0);
                EmbedBuilder bld = recreateEmbed(sentEmbed);

                int voted = 0;
                for (IReaction r : message.getReactions()) {
                    voted += r.getUsers().size() - 1;
                }
                int staffCount = event.getGuild().getUsersByRole(staff).size();

                bld.appendField("Staff Reacted: ", voted + "/" + staffCount, false);

                RequestBuffer.request(() -> message.edit(bld.build()));
            }
        } else {
            //deletes reactions that aren't real votes
            removeReaction(message, user, event.getReaction());
        }
    }

    private void removeReaction(IMessage message, IUser user, IReaction reaction) {
        try {
            RequestBuffer.request(() -> message.removeReaction(user, reaction));
        } catch (DiscordException | MissingPermissionsException e) {
            Util.reportHome(e);
        }
    }

    private boolean hasReacted(IMessage message, IUser user, IReaction iReaction) {
        List<IReaction> reactions = message.getReactions().stream().filter(reaction -> reaction != iReaction).collect(Collectors.toList());
        for (IReaction r : reactions) {
            if (r.getUserReacted(user)) {
                return true;
            }
        }
        return false;
    }

    private EmbedBuilder recreateEmbed(IEmbed embed) {
        EmbedBuilder bld = new EmbedBuilder();

        bld
                .withDesc(embed.getDescription())
                .withFooterIcon(embed.getFooter().getIconUrl())
                .withFooterText(embed.getFooter().getText())
                .withTimestamp(embed.getTimestamp())
                .withColor(Color.ORANGE);

        return bld;
    }
}
