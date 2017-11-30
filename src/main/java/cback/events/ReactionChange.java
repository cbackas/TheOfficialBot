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
                .withColor(Color.ORANGE);

        return bld;
    }

    /*@EventSubscriber
    public void starboardReactionAdd(ReactionAddEvent event) {
        if (event.getUser().isBot() || event.getChannel().getLongID() == OfficialBot.ANNOUNCEMENT_CH_ID) {
            return; //ignores bot reactions and reactions in the announcement channel
        }
        IChannel starboard = event.getClient().getChannelByID(OfficialBot.STARBOARD_CH_ID);

        IMessage message = event.getMessage();
        if (message.getChannel().getLongID() != OfficialBot.STARBOARD_CH_ID) { //for all channels that aren't the actual starboard channel
            String emojiName = event.getReaction().getEmoji().getName();
            if (emojiName.equals("⭐️")) {
                ReactionEmoji emoji = event.getReaction().getEmoji();
                int count = message.getReactionByEmoji(emoji).getUsers().size();
                if (count >= 3) {

                    IMessage starMessage = null;
                    for (IMessage m : starboard.getMessageHistory().asArray()) {
                        if (m.getContent().contains(message.getStringID())) {
                            starMessage = m;
                        }
                    }

                    if (starMessage == null) {
                        createStarPost(message, count);
                    } else if (count >= 3 && count < 5) {
                        updateLvl1(starMessage, count);
                    } else if (count >= 5 && count < 10) {
                        updateLvl2(starMessage, count);
                    } else if (count >= 10 && count < 25) {
                        updateLvl3(starMessage, count);
                    } else if (count >= 25) {
                        updateLvl4(starMessage, count);
                    }
                }
            }
        }
    }


    @EventSubscriber
    public void noticeboardReactionRemover(ReactionAddEvent event) {
        if (event.getMessageID() == 318100010518446080L) {
            IReaction reaction = event.getReaction();
            RequestBuffer.request(() -> event.getMessage().removeReaction(event.getUser(), reaction));
        }
    }

    private void createStarPost(IMessage m, int count) {
        EmbedBuilder bld = new EmbedBuilder()
                .withAuthorIcon(m.getAuthor().getAvatarURL())
                .withColor(OfficialBot.getBotColor())
                .withAuthorName(m.getAuthor().getDisplayName(m.getGuild()))
                .withDescription(m.getFormattedContent())
                .withTimestamp(m.getTimestamp());

        try {
            RequestBuffer.request(() ->
                    m.getClient().getChannelByID(OfficialBot.STARBOARD_CH_ID).sendMessage(
                            ":star: " + count + " in " + m.getChannel() + " (" + m.getStringID() + ")",
                            bld.build()));
        } catch (MissingPermissionsException | DiscordException e) {
            Util.reportHome(e);
        }
    }

    private void updateLvl1(IMessage starPost, int count) {
        IEmbed embed = starPost.getEmbeds().get(0);
        EmbedBuilder bld = new EmbedBuilder()
                .withAuthorIcon(embed.getAuthor().getIconUrl())
                .withColor(OfficialBot.getBotColor())
                .withAuthorName(embed.getAuthor().getName())
                .withDescription(embed.getDescription())
                .withTimestamp(embed.getTimestamp());

        try {
            RequestBuffer.request(() ->
                    starPost.edit(":star: " + count + " in " + starPost.getChannel() + " (" + starPost.getStringID() + ")", bld.build()));
        } catch (MissingPermissionsException | DiscordException e) {
            Util.reportHome(e);
        }
    }

    private void updateLvl2(IMessage starPost, int count) {
        IEmbed embed = starPost.getEmbeds().get(0);
        EmbedBuilder bld = new EmbedBuilder()
                .withAuthorIcon(embed.getAuthor().getIconUrl())
                .withColor(OfficialBot.getBotColor())
                .withAuthorName(embed.getAuthor().getName())
                .withDescription(embed.getDescription())
                .withTimestamp(embed.getTimestamp());

        try {
            RequestBuffer.request(() ->
                    starPost.edit(":star2: " + count + " in " + starPost.getChannel() + " (" + starPost.getStringID() + ")", bld.build()));
        } catch (MissingPermissionsException | DiscordException e) {
            Util.reportHome(e);
        }
    }

    private void updateLvl3(IMessage starPost, int count) {
        IEmbed embed = starPost.getEmbeds().get(0);
        EmbedBuilder bld = new EmbedBuilder()
                .withAuthorIcon(embed.getAuthor().getIconUrl())
                .withColor(OfficialBot.getBotColor())
                .withAuthorName(embed.getAuthor().getName())
                .withDescription(embed.getDescription())
                .withTimestamp(embed.getTimestamp());

        try {
            RequestBuffer.request(() ->
                    starPost.edit(":star2: " + count + " in " + starPost.getChannel() + " (" + starPost.getStringID() + ")", bld.build()));
        } catch (MissingPermissionsException | DiscordException e) {
            Util.reportHome(e);
        }
    }

    private void updateLvl4(IMessage starPost, int count) {
        IEmbed embed = starPost.getEmbeds().get(0);
        EmbedBuilder bld = new EmbedBuilder()
                .withAuthorIcon(embed.getAuthor().getIconUrl())
                .withColor(OfficialBot.getBotColor())
                .withAuthorName(embed.getAuthor().getName())
                .withDescription(embed.getDescription())
                .withTimestamp(embed.getTimestamp());

        try {
            RequestBuffer.request(() ->
                    starPost.edit(":sparkles: " + count + " in " + starPost.getChannel() + " (" + starPost.getStringID() + ")", bld.build()));
        } catch (MissingPermissionsException | DiscordException e) {
            Util.reportHome(e);
        }
    }*/
}
