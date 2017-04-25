package cback.commands;

import cback.OfficialRoles;
import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return "?userinfo @user";
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
    public void execute(TheOfficialBot bot, IDiscordClient client, String[] args, IGuild guild, List<Long> roleIDs, IMessage message, boolean isPrivate) {
        if (!Collections.disjoint(roleIDs, getPermissions())) {

            String text = message.getContent();
            Pattern pattern = Pattern.compile("^\\?user <@!?(\\d+)>");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                IUser user = guild.getUserByID(Long.parseLong(matcher.group(1)));
                String isBot = user.isBot() ? "yes":"no";
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
                List<IRole> roles = user.getRolesForGuild(guild);

                EmbedBuilder embed = new EmbedBuilder();

                embed
                        .withAuthorIcon(user.getAvatarURL())
                        .withAuthorName(Util.getTag(user))
                        .withDesc("\uD83C\uDD94: ``" + user.getStringID() + "``"
                        + "\n\uD83E\uDD16 **Bot**: ``" + isBot + "``"
                        + "\n\uD83D\uDCE5 **Joined Server**: ``" + guild.getJoinTimeForUser(user).format(formatter) + "``"
                        + "\n\uD83C\uDF10 **Joined Discord**: ``" + user.getCreationDate().format(formatter) + "``"
                        + "\n\u2139 **Status**: ``" + user.getPresence().toString() + "``"
                        + "\n\uD83D\uDEE1 **Roles**: ``" + roles.size() + " - " + roles.toString() + "``")
                        .withFooterIcon(message.getAuthor().getAvatarURL())
                        .withFooterText("Requested by: " + Util.getTag(message.getAuthor()))
                        .withColor(Color.gray);

                Util.sendEmbed(message.getChannel(), embed.build());
            }
        }
    }
}
