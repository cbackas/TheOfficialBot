package cback.commands;

import cback.TheOfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHelp implements Command {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("commands");
    }

    @Override
    public String getSyntax() {
        return "?help";
    }

    @Override
    public String getDescription() {
        return "Returns a list of commands (you're looking at it right now)";
    }

    @Override
    public List<Long> getPermissions() {
        return null;
    }

    @Override
    public void execute(TheOfficialBot bot, IDiscordClient client, String[] args, IGuild guild, List<Long> roleIDs, IMessage message, boolean isPrivate) {

        EmbedBuilder embed = Util.getEmbed();
        embed.withTitle("Commands:");

        List<String> roles = message.getAuthor().getRolesForGuild(guild).stream().map(role -> role.getStringID()).collect(Collectors.toList());
        for (Command c : TheOfficialBot.registeredCommands) {

            if (c.getDescription() != null) {

                String aliases = "";
                if (c.getAliases() != null) {
                    aliases = "\n*Aliases:* " + c.getAliases().toString();
                }

                if (c.getPermissions() == null) {
                    embed.appendField(c.getSyntax(), c.getDescription() + aliases, false);
                } else if (!Collections.disjoint(roles, c.getPermissions())) {
                    embed.appendField(c.getSyntax(), c.getDescription() + aliases, false);
                }

            }

        }

        embed.withFooterText("Staff commands excluded for regular users");

        try {
            Util.sendEmbed(message.getAuthor().getOrCreatePMChannel(), embed.withColor(161, 61, 61).build());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Util.deleteMessage(message);

    }

}
