package cback.events;

import cback.OfficialBot;
import cback.Util;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.*;
import sx.blah.discord.handle.impl.events.guild.member.NicknameChangedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class MessageChange {
    private OfficialBot bot;

    public MessageChange(OfficialBot bot) {
        this.bot = bot;
    }

    @EventSubscriber
    public void messageDeleted(MessageDeleteEvent event) {
        if (event.getGuild().getStringID().equals(OfficialBot.getHomeGuild().getStringID()) && event.getMessage() != null) {
            if (!event.getAuthor().isBot() && !OfficialBot.messageCache.contains(event.getMessageID())) {
                if (event.getChannel().getLongID() != OfficialBot.STAFF_CH_ID && event.getChannel().getLongID() != OfficialBot.ADMIN_CH_ID && event.getChannel().getLongID() != OfficialBot.DEV_CH_ID) {
                    OfficialBot.messageCache.remove(event.getMessageID());
                    IMessage message = event.getMessage();
                    IUser author = event.getAuthor();
                    IChannel channel = event.getChannel();

                    Boolean tripped = true;
                    for (String p : bot.prefixes) {
                        if (message.getContent().startsWith(p)) {
                            tripped = false;
                        }
                    }

                    if (tripped) {
                        EmbedBuilder bld = new EmbedBuilder()
                                .withAuthorName(author.getName() + "#" + author.getDiscriminator())
                                .withAuthorIcon(Util.getAvatar(author))
                                .withDesc("**Message sent by **" + author.mention() + "** deleted in **" + channel.mention() + "\n" + message.getContent())
                                .withFooterText("User ID: " + author.getStringID())
                                .withTimestamp(System.currentTimeMillis())
                                .withColor(OfficialBot.getBotColor());

                        IChannel MESSAGE_LOGS = event.getClient().getChannelByID(OfficialBot.MESSAGELOG_CH_ID);
                        Util.sendEmbed(MESSAGE_LOGS, bld.build());
                    }
                }
            }
        }
    }

    @EventSubscriber
    public void messageEdited(MessageUpdateEvent event) {
        if (event instanceof  MessagePinEvent || event instanceof MessageUnpinEvent) {
            return;
        }
        
        if (event.getGuild().getStringID().equals(OfficialBot.getHomeGuild().getStringID()) && event.getMessage() != null) {
            if (!event.getAuthor().isBot()) {
                IMessage message = event.getMessage();
                IMessage oldMessage = event.getOldMessage();
                IMessage newMessage = event.getNewMessage();
                IUser author = event.getAuthor();
                IChannel channel = event.getChannel();

                EmbedBuilder bld = new EmbedBuilder().withColor(java.awt.Color.decode("#FFA500"));
                bld
                        .withAuthorName(author.getName() + "#" + author.getDiscriminator())
                        .withAuthorIcon(Util.getAvatar(author))
                        .withDesc("**Message Edited in **" + channel.mention())
                        .appendField("Before", oldMessage.getContent(), false)
                        .appendField("After", newMessage.getContent(), false)
                        .withFooterText("ID: " + message.getStringID())
                        .withTimestamp(System.currentTimeMillis());

                IChannel MESSAGE_LOGS = event.getClient().getChannelByID(OfficialBot.MESSAGELOG_CH_ID);
                Util.sendEmbed(MESSAGE_LOGS, bld.build());
                bot.censorMessages(message);
            }
        }
    }

    @EventSubscriber
    public void nicknameChange(NicknameChangedEvent event) {
        if (event.getGuild().getStringID().equals(OfficialBot.getHomeGuild().getStringID())) {
            IUser user = event.getUser();

            String oldName = event.getUser().getName();
            if (event.getOldNickname().isPresent()) {
                oldName = event.getOldNickname().get();
            }

            String newName = event.getUser().getDisplayName(OfficialBot.getHomeGuild());
            if (event.getNewNickname().isPresent()) {
                newName = event.getNewNickname().get();
            }

            EmbedBuilder bld = new EmbedBuilder().withColor(java.awt.Color.decode("#FFA500"));
            bld
                    .withAuthorName(user.getName() + "#" + user.getDiscriminator())
                    .withAuthorIcon(Util.getAvatar(user))
                    .withDesc(user.mention() + " **nickname changed**")
                    .appendField("Before", oldName, false)
                    .appendField("After", newName, false)
                    .withFooterText("ID: " + user.getStringID())
                    .withTimestamp(System.currentTimeMillis());

            IChannel MESSAGE_LOGS = event.getClient().getChannelByID(OfficialBot.MESSAGELOG_CH_ID);
            Util.sendEmbed(MESSAGE_LOGS, bld.build());
        }
    }

}
