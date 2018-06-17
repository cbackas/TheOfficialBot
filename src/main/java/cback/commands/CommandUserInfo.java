package cback.commands;

import cback.OfficialBot;
import cback.OfficialRoles;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandUserInfo implements Command {
    @Override
    public String getName() {
        return "userinfo";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("user");
    }

    @Override
    public String getSyntax() {
        return "userinfo @user";
    }

    @Override
    public String getDescription() {
        return "Returns information about a discord user";
    }

    @Override
    public List<Long> getPermissions() {
        return Arrays.asList(OfficialRoles.STAFF.id);
    }

    @Override
    public void execute(IMessage message, String content, String[] args, IUser author, IGuild guild, List<Long> roleIDs, boolean isPrivate, IDiscordClient client, OfficialBot bot) {
        if (args.length == 1) {
            Pattern pattern = Pattern.compile("^<@!?(\\d+)>");
            Matcher matcher = pattern.matcher(args[0]);
            if (matcher.find()) {
                IUser user = client.getUserByID(Long.parseLong(matcher.group(1)));

                String isBot;
                try {
                    isBot = user.isBot() ? "yes" : "no";
                } catch (Exception e) {
                    Util.simpleEmbed(message.getChannel(), "Error: The bot doesn't share a server with this user.", Color.red);
                    return;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

                EmbedBuilder embed = new EmbedBuilder();

                embed
                        .withAuthorIcon(user.getAvatarURL())
                        .withAuthorName(Util.getTag(user))
                        .withDesc("\uD83C\uDD94: ``" + user.getStringID() + "``"
                                + "\n\uD83E\uDD16 **Bot**: ``" + isBot + "``"
                                + "\n\uD83D\uDCE5 **Joined Server**: ``" + guild.getJoinTimeForUser(user).atOffset(ZoneOffset.ofHours(0)).format(formatter) + "``"
                                + "\n\uD83C\uDF10 **Joined Discord**: ``" + user.getCreationDate().atOffset(ZoneOffset.ofHours(0)).format(formatter) + "``"
                                + "\n\u2139 **Status**: ``" + user.getPresence().getStatus().name() + "``"
                                + "\n\uD83D\uDEE1 **Roles**: ``" + roleList(user, guild) + "``")
                        .withFooterIcon(message.getAuthor().getAvatarURL())
                        .withFooterText("Requested by: " + Util.getTag(message.getAuthor()))
                        .withColor(Color.gray);

                Util.sendEmbed(message.getChannel(), embed.build());
            }
        } else {
            Util.syntaxError(this, message);
        }
    }

    private String roleList(IUser user, IGuild guild) {
        List<IRole> roles = user.getRolesForGuild(guild);
        String output = roles.size() + " - ";

        List<String> roleNames = roles.stream().map(r -> r.getName()).collect(Collectors.toList());
        String roleList = String.join(", ", roleNames);

        output += roleList;

        return output;
    }
}
