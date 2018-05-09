package cback;

import cback.commands.Command;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Util {
    private static final Pattern USER_MENTION_PATTERN = Pattern.compile("^<@!?(\\d+)>$");

    static IDiscordClient client = OfficialBot.getInstance().getClient();
    static Color BOT_COLOR = OfficialBot.getBotColor();

    public static void sendMessage(IChannel channel, String message) {
        try {
            channel.sendMessage(message);
        } catch (Exception e) {
            reportHome(e);
        }
    }

    public static IMessage sendEmbed(IChannel channel, EmbedObject embedObject) {
        RequestBuffer.RequestFuture<IMessage> future = RequestBuffer.request(() -> {
            try {
                return new MessageBuilder(OfficialBot.getInstance().getClient()).withEmbed(embedObject)
                        .withChannel(channel).send();
            } catch (MissingPermissionsException | DiscordException e) {
                reportHome(e);
            }
            return null;
        });
        return future.get();
    }

    public static IMessage sendBufferedMessage(IChannel channel, String message) {
        RequestBuffer.RequestFuture<IMessage> sentMessage = RequestBuffer.request(() -> {
            try {
                return channel.sendMessage(message);
            } catch (MissingPermissionsException | DiscordException e) {
                reportHome(e);
            }
            return null;
        });
        return sentMessage.get();
    }

    public static void deleteMessage(IMessage message) {
        RequestBuffer.request(() -> {
            try {
                message.delete();
            } catch (MissingPermissionsException | DiscordException e) {
                reportHome(e);
            }
        });
    }

    public static void bulkDelete(IChannel channel, List<IMessage> toDelete) {
        RequestBuffer.request(() -> {
            if (toDelete.size() > 0) {
                if (toDelete.size() == 1) {
                    try {
                        toDelete.get(0).delete();
                    } catch (MissingPermissionsException | DiscordException e) {
                        reportHome(e);
                    }
                } else {
                    try {
                        channel.bulkDelete(toDelete);
                    } catch (DiscordException | MissingPermissionsException e) {
                        reportHome(e);
                    }

                }
            }
        });
    }

    public static void sendAnnouncement(String message) {
        try {
            Util.sendMessage(OfficialBot.getInstance().getClient().getChannelByID(OfficialBot.GENERAL_CH_ID), message);
            Util.sendMessage(OfficialBot.getInstance().getClient().getChannelByID(OfficialBot.ANNOUNCEMENT_CH_ID), message);
        } catch (Exception e) {
            reportHome(e);
        }
    }

    /**
     * Private messages
     */
    public static void sendPrivateMessage(IUser user, String message) {
        try {
            user.getClient().getOrCreatePMChannel(user).sendMessage(message);
        } catch (Exception e) {
            if (!e.toString().equals("sx.blah.discord.util.DiscordException: Message was unable to be sent (Discord didn't return a response).")) {
                reportHome(e);
            }
        }
    }

    public static void sendPrivateEmbed(IUser user, String message) {
        try {
            IChannel pmChannel = user.getClient().getOrCreatePMChannel(user);
            simpleEmbed(pmChannel, message);
        } catch (Exception e) {
            reportHome(e);
        }
    }

    //EMBEDBUILDER STUFF
    public static EmbedBuilder getEmbed() {
        return new EmbedBuilder()
                .withAuthorIcon(getAvatar(OfficialBot.getInstance().getClient().getOurUser()))
                .withAuthorUrl("https://github.com/cback")
                .withAuthorName(getTag(OfficialBot.getInstance().getClient().getOurUser()));
    }

    /**
     * Returns clean little tag to use to talk about users in a very accurate and descriptive manor
     */
    public static String getTag(IUser user) {
        return user.getName() + '#' + user.getDiscriminator();
    }

    public static EmbedBuilder getEmbed(IUser user) {
        return getEmbed().withFooterIcon(getAvatar(user))
                .withFooterText("Requested by @" + getTag(user));
    }

    public static String getAvatar(IUser user) {
        return user.getAvatar() != null ? user.getAvatarURL() : "https://discordapp.com/assets/322c936a8c8be1b803cd94861bdfa868.png";
    }

    public static int getCurrentTime() {
        return Math.toIntExact(System.currentTimeMillis() / 1000);
    }

    public static IUser getUserFromMentionArg(String arg) {
        Matcher matcher = USER_MENTION_PATTERN.matcher(arg);
        if (matcher.matches()) {
            return OfficialBot.getInstance().getClient().getUserByID(Long.parseLong(matcher.group(1)));
        }
        return null;
    }

    /**
     * Send report
     */
    public static void reportHome(IMessage message, Exception e) {
        e.printStackTrace();

        IChannel errorChannel = client.getChannelByID(OfficialBot.ERRORLOG_CH_ID);

        EmbedBuilder bld = new EmbedBuilder()
                .withColor(BOT_COLOR)
                .withTimestamp(System.currentTimeMillis())
                .withAuthorName(message.getAuthor().getName() + '#' + message.getAuthor().getDiscriminator())
                .withAuthorIcon(getAvatar(message.getAuthor()))
                .withDesc(message.getContent())
                .appendField("\u200B", "\u200B", false)

                .appendField("Exeption:", e.toString(), false);

        StringBuilder stack = new StringBuilder();
        for (StackTraceElement s : e.getStackTrace()) {
            stack.append(s.toString());
            stack.append("\n");
        }

        String stackString = stack.toString();
        if (stackString.length() > 800) {
            stackString = stackString.substring(0, 800);
        }

        bld
                .appendField("Stack:", stackString, false);

        sendEmbed(errorChannel, bld.build());
    }

    public static void reportHome(Exception e) {
        e.printStackTrace();

        IChannel errorChannel = client.getChannelByID(OfficialBot.ERRORLOG_CH_ID);

        EmbedBuilder bld = new EmbedBuilder()
                .withColor(BOT_COLOR)
                .withTimestamp(System.currentTimeMillis())
                .appendField("Exeption:", e.toString(), false);

        StringBuilder stack = new StringBuilder();
        for (StackTraceElement s : e.getStackTrace()) {
            stack.append(s.toString());
            stack.append("\n");
        }

        String stackString = stack.toString();
        if (stackString.length() > 800) {
            stackString = stackString.substring(0, 800);
        }

        bld
                .appendField("Stack:", stackString, false);

        sendEmbed(errorChannel, bld.build());
    }

    /**
     * Send botLog
     */
    public static void botLog(IMessage message) {
        try {
            IChannel botLogChannel = client.getChannelByID(OfficialBot.BOTLOG_CH_ID);

            EmbedBuilder bld = new EmbedBuilder()
                    .withColor(BOT_COLOR)
                    .withAuthorName(message.getAuthor().getName() + '#' + message.getAuthor().getDiscriminator())
                    .withAuthorIcon(getAvatar(message.getAuthor()))
                    .withDesc(message.getFormattedContent())
                    .withFooterText(message.getGuild().getName() + "/#" + message.getChannel().getName())
                    .withTimestamp(System.currentTimeMillis());

            sendEmbed(botLogChannel, bld.build());
        } catch (Exception e) {
            reportHome(message, e);
        }
    }

    /**
     * Command syntax error
     */
    public static void syntaxError(Command command, IMessage message) {
        try {
            EmbedBuilder bld = new EmbedBuilder()
                    .withColor(BOT_COLOR)
                    .withAuthorName(command.getName())
                    .withAuthorIcon(client.getApplicationIconURL())
                    .withDesc(command.getDescription())
                    .appendField("Syntax:", OfficialBot.getPrefix() + command.getSyntax(), false);

            sendEmbed(message.getChannel(), bld.build());
        } catch (Exception e) {
            reportHome(e);
        }
    }

    /**
     * Send simple fast embeds
     */
    public static void simpleEmbed(IChannel channel, String message) {
        sendEmbed(channel, new EmbedBuilder().withDescription(message).withColor(BOT_COLOR).build());
    }

    public static void simpleEmbed(IChannel channel, String message, Color color) {
        sendEmbed(channel, new EmbedBuilder().withDescription(message).withColor(color).build());
    }

    //checks for dirty words
    public static boolean censorMessages(IMessage message) {
        boolean homeGuild = message.getGuild().getLongID() == OfficialBot.HOME_GUILD_ID;
        boolean staffChannel = message.getChannel().getCategory().getStringID().equals(OfficialBot.STAFF_CAT_ID);
        boolean staffMember = message.getAuthor().hasRole(message.getClient().getRoleByID(OfficialRoles.STAFF.id));
        if (homeGuild && !staffChannel && !staffMember) {
            String content = message.getFormattedContent().toLowerCase();

            String word = checkMessage(content);

            if (word != null) {
                IUser author = message.getAuthor();

                EmbedBuilder bld = new EmbedBuilder();
                bld
                        .withAuthorIcon(author.getAvatarURL())
                        .withAuthorName(Util.getTag(author))
                        .withDesc(message.getFormattedContent())
                        .withTimestamp(System.currentTimeMillis())
                        .withFooterText("Auto-deleted from #" + message.getChannel().getName());

                Util.sendEmbed(message.getGuild().getChannelByID(OfficialBot.MESSAGELOG_CH_ID), bld.withColor(OfficialBot.getBotColor()).build());

                StringBuilder sBld = new StringBuilder().append("Your message has been automatically removed for containing a banned word. If this is an error, message a staff member.");

                sBld
                        .append("\n\n")
                        .append(word);

                Util.sendPrivateEmbed(author, sBld.toString());

                OfficialBot.messageCache.add(message.getLongID());
                Util.deleteMessage(message);
                return true;
            }
        }
        return false;
    }

    public static String checkMessage(String text) {
        List<String> bannedWords = OfficialBot.getInstance().getConfigManager().getConfigArray("bannedWords");

        String word = "";
        for (String w : bannedWords) {
            if (text.matches("\\n?.*\\b\\n?" + w + "\\n?\\b.*\\n?.*") || text.matches("\\n?.*\\b\\n?" + w + "s\\n?\\b.*\\n?.*")) {
                word = w;
                break;
            }
        }
        if (word.isEmpty()) {
            return null;
        } else {
            return word;
        }
    }
}
