package cback.events;

import cback.OfficialBot;
import cback.Util;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.ChannelCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.RequestBuffer;

import java.util.EnumSet;
import java.util.List;

public class ChannelChange {
    private OfficialBot bot;

    public ChannelChange(OfficialBot bot) {
        this.bot = bot;
    }

    @EventSubscriber //Set all
    public void setMuteRoleMASS(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        String text = message.getContent();
        IDiscordClient client = event.getClient();
        if (text.equalsIgnoreCase("?setmuteperm") && message.getAuthor().getStringID().equals("73416411443113984")) {
            IGuild guild = client.getGuildByID(Long.parseLong("266649217538195457"));
            List<IChannel> channelList = guild.getChannels();
            IRole muted = guild.getRoleByID(Long.parseLong("281022564002824192"));
            for (IChannel channels : channelList) {
                RequestBuffer.request(() -> {
                    try {
                        channels.overrideRolePermissions(muted, EnumSet.noneOf(Permissions.class), EnumSet.of(Permissions.SEND_MESSAGES));
                    } catch (Exception e) {
                        Util.reportHome(e);
                    }
                });
            }
            System.out.println("Set muted role");
            Util.deleteMessage(message);
        }
    }

    @EventSubscriber //New Channel
    public void newChannel(ChannelCreateEvent event) {
        if (event.getGuild().getStringID().equals(OfficialBot.getHomeGuild().getStringID())) {
            //Set muted role
            IGuild guild = OfficialBot.getHomeGuild();
            IRole muted = guild.getRoleByID(Long.parseLong("281022564002824192"));
            try {
                event.getChannel().overrideRolePermissions(muted, EnumSet.noneOf(Permissions.class), EnumSet.of(Permissions.SEND_MESSAGES));
            } catch (Exception e) {
                Util.reportHome(e);
            }
        }
    }
}


