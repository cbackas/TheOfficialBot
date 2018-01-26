package cback;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.*;

import java.awt.*;

public class ServerLog {
    String logText;
    IUser mod;
    Color color = Color.gray;

    public ServerLog (IUser mod, String logText) {
        this.mod = mod;
        this.logText = logText;
    }

    public ServerLog (IUser mod, String logText, Color color) {
        this.mod = mod;
        this.logText = logText;
        this.color = color;
    }

    public IMessage send() {
        RequestBuffer.RequestFuture<IMessage> future = RequestBuffer.request(() -> {
            try {
                new EmbedBuilder();
                EmbedBuilder embed = new EmbedBuilder().withColor(this.color);

                embed
                        .withFooterIcon(Util.getAvatar(this.mod))
                        .withFooterText("Action by @" + Util.getTag(this.mod))
                        .withDescription(this.logText)
                        .withTimestamp(System.currentTimeMillis());

                IDiscordClient client = OfficialBot.getInstance().getClient();
                return new MessageBuilder(client)
                        .withEmbed(embed.build())
                        .withChannel(client.getChannelByID(OfficialBot.SERVERLOG_CH_ID))
                        .send();
            } catch (MissingPermissionsException | DiscordException e) {
                Util.reportHome(e);
            }
            return null;
        });
        return future.get();
    }
}
