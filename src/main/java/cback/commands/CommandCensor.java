package cback.commands;

import cback.OfficialRoles;
import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Arrays;
import java.util.List;

public class CommandCensor implements Command {
    @Override
    public String getName() {
        return "censor";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "?censor add|remove [word]";
    }

    @Override
    public String getDescription() {
        return "adds or removes a word to be censored from the server";
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList(OfficialRoles.ADMIN.id);
    }

    @Override
    public void execute(TheOfficialBot bot, IDiscordClient client, String[] args, IGuild guild, IMessage message, boolean isPrivate) {
        if (message.getAuthor().getRolesForGuild(guild).contains(guild.getRoleByID(Long.parseLong(OfficialRoles.ADMIN.id)))) {
            message.getChannel().setTypingStatus(true);

            EmbedBuilder bld = new EmbedBuilder();
            List<String> bannedWords = bot.getConfigManager().getConfigArray("bannedWords");

            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("add")) {
                    if (bannedWords.contains(args[1])) {
                        bld.withDesc(args[1] + " is already a banned word!");
                    } else {
                        bannedWords.add(args[1]);
                        bot.getConfigManager().setConfigValue("bannedWords", bannedWords);

                        bld.withDesc(args[1] + " has been added to the list of banned words.");
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (bannedWords.contains(args[1])) {
                        bannedWords.remove(args[1]);
                        bot.getConfigManager().setConfigValue("bannedWords", bannedWords);

                        bld.withDesc(args[1] + " has been removed from the list of banned words.");
                    } else {
                        bld.withDesc(args[1] + " is not a censored word... Remove failed.");
                    }
                } else {
                    bld.appendField(getSyntax(), getDescription(), false);
                }
            } else if (args.length == 1 && args[0].equals("list")) {
                String sexyList = "";

                for (String s: bannedWords) {
                    sexyList = sexyList + s + "\n";
                }

                bld.withDesc(sexyList);
            } else {
                bld.appendField(getSyntax(), getDescription(), false);
            }

            Util.sendEmbed(message.getChannel(), bld.withColor(161, 61, 61).build());
            message.getChannel().setTypingStatus(false);

        }
    }
}