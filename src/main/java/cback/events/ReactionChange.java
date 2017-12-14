package cback.events;

import cback.OfficialBot;
import cback.OfficialRoles;
import cback.Util;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    //required reactions to make different starboard things happen
    final int base = 5;
    final int level2 = 10;
    final int level3 = 15;
    final int level4 = 20;

    @EventSubscriber
    public void starboardReactionAdd(ReactionAddEvent event) {
        if (event.getUser().isBot() || event.getChannel().getLongID() == OfficialBot.ANNOUNCEMENT_CH_ID) {
            return; //ignores bot reactions and reactions in the announcement channel
        }
        IChannel starboard = event.getClient().getChannelByID(OfficialBot.STARBOARD_CH_ID);

        IMessage message = event.getMessage();
        if (message.getChannel().getLongID() != OfficialBot.STARBOARD_CH_ID) { //for all channels that aren't the actual starboard channel
            ReactionEmoji emoji = event.getReaction().getEmoji();
            String emojiName = emoji.getName();
            if (emojiName.equals("⭐")) {
                int count = message.getReactionByEmoji(emoji).getUsers().stream().filter(u -> !u.isBot()).collect(Collectors.toList()).size(); //filters out bot reactions from the count

                IMessage starMessage = null;
                for (IMessage m : starboard.getFullMessageHistory().asArray()) {
                    if (m.getContent().contains(message.getStringID())) {
                        starMessage = m;
                    }
                }

                if (count >= base) {
                    if (starMessage == null) {
                        createStarPost(message, count);
                    } else {
                        updateStarPost(message, starMessage, count);
                    }
                } else {
                    if (starMessage != null) {
                        Util.deleteMessage(starMessage);
                    }
                }
            }
        }
    }

    @EventSubscriber
    public void starboardReactionRemove(ReactionRemoveEvent event) {
        if (event.getUser().isBot() || event.getChannel().getLongID() == OfficialBot.ANNOUNCEMENT_CH_ID) {
            return; //ignores bot reactions and reactions in the announcement channel
        }
        IChannel starboard = event.getClient().getChannelByID(OfficialBot.STARBOARD_CH_ID);

        IMessage message = event.getMessage();
        if (message.getChannel().getLongID() != OfficialBot.STARBOARD_CH_ID) {
            ReactionEmoji emoji = event.getReaction().getEmoji();
            String emojiName = emoji.getName();
            if (emojiName.equals("⭐")) {
                int count = message.getReactionByEmoji(emoji).getUsers().stream().filter(u -> !u.isBot()).collect(Collectors.toList()).size(); //filters out bot reactions from the count

                IMessage starMessage = null;
                for (IMessage m : starboard.getFullMessageHistory().asArray()) {
                    if (m.getContent().contains(message.getStringID())) {
                        starMessage = m;
                    }
                }


                if (count >= base) {
                    if (starMessage == null) {
                        createStarPost(message, count);
                    } else {
                        updateStarPost(message, starMessage, count);
                    }
                } else {
                    if (starMessage != null) {
                        Util.deleteMessage(starMessage);
                    }
                }
            }
        }
    }

    private void createStarPost(IMessage m, int count) {
        EmbedBuilder bld = new EmbedBuilder()
                .withAuthorIcon(m.getAuthor().getAvatarURL())
                .withColor(Color.ORANGE)
                .withAuthorName(m.getAuthor().getDisplayName(m.getGuild()))
                .withDescription(m.getFormattedContent())
                .withTimestamp(m.getTimestamp());

        for (IMessage.Attachment a : m.getAttachments()) {
            bld.withImage(a.getUrl());
        }

        try {
            RequestBuffer.request(() ->
                    m.getClient().getChannelByID(OfficialBot.STARBOARD_CH_ID).sendMessage(
                            ":star: " + count + " in " + m.getChannel() + " (" + m.getStringID() + ")",
                            bld.build()));
        } catch (MissingPermissionsException | DiscordException e) {
            Util.reportHome(e);
        }
    }

    private void updateStarPost(IMessage originMessage, IMessage starPost, int count) {
        String starEmoji = ":star:";
        if (count >= level2 && count < level3) {
            starEmoji = ":star2:";
        } else if (count >= level3 && count < level4) {
            starEmoji = ":stars:";
        } else if (count >= level4) {
            starEmoji = ":sparkles:";
        }

        String channelID = originMessage.getChannel().getStringID();
        String messageID = originMessage.getStringID();

        EmbedBuilder bld = new EmbedBuilder()
                .withAuthorIcon(originMessage.getAuthor().getAvatarURL())
                .withColor(Color.ORANGE)
                .withAuthorName(originMessage.getAuthor().getDisplayName(originMessage.getGuild()))
                .withDescription(originMessage.getFormattedContent())
                .withTimestamp(originMessage.getTimestamp());

        for (IMessage.Attachment a : originMessage.getAttachments()) {
            bld.withImage(a.getUrl());
        }

        String text = starEmoji + " " + count + " in <#" + channelID + "> (" + messageID + ")";
        try {
            RequestBuffer.request(() -> starPost.edit(text, bld.build()));
        } catch (MissingPermissionsException | DiscordException e) {
            Util.reportHome(e);
        }
    }
}
