package cback;

import cback.commands.Command;
import cback.events.ChannelChange;
import cback.events.MemberChange;
import org.reflections.Reflections;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.modules.Configuration;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("FieldCanBeLocal")
public class TheOfficialBot {

    private static TheOfficialBot instance;
    private IDiscordClient client;

    private ConfigManager configManager;
    private Scheduler scheduler;

    private List<String> botAdmins = new ArrayList<>();
    public static List<Command> registeredCommands = new ArrayList<>();

    private static final Pattern COMMAND_PATTERN = Pattern.compile("^\\?([^\\s]+) ?(.*)", Pattern.CASE_INSENSITIVE);
    public static final String ANNOUNCEMENT_CHANNEL_ID = "318098998047277057";
    public static final String GENERAL_CHANNEL_ID = "266649217538195457";
    public static final String LOG_CHANNEL_ID = "281021113440534528";

    private long startTime;

    public static void main(String[] args) {
        new TheOfficialBot();
    }

    public TheOfficialBot() {

        instance = this;

        //instantiate config manager first as connect() relies on tokens
        configManager = new ConfigManager(this);

        connect();
        client.getDispatcher().registerListener(this);
        client.getDispatcher().registerListener(new ChannelChange(this));
        client.getDispatcher().registerListener(new MemberChange(this));

        scheduler = new Scheduler(this);

        registerAllCommands();

        botAdmins.add("73416411443113984");
        botAdmins.add("224625782188670986");

    }

    private void connect() {
        //don't load external modules and don't attempt to create modules folder
        Configuration.LOAD_EXTERNAL_MODULES = false;

        Optional<String> token = configManager.getTokenValue("botToken");
        if (!token.isPresent()) {
            System.out.println("-------------------------------------");
            System.out.println("Insert your bot's token in the config.");
            System.out.println("Exiting......");
            System.out.println("-------------------------------------");
            System.exit(0);
            return;
        }

        ClientBuilder clientBuilder = new ClientBuilder();
        clientBuilder.withToken(token.get());
        clientBuilder.setMaxReconnectAttempts(5);
        try {
            client = clientBuilder.login();
        } catch (DiscordException e) {
            e.printStackTrace();
        }
    }

    @EventSubscriber
    public void onMessageEvent(MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) return; //ignore bot messages
        IMessage message = event.getMessage();
        IGuild guild = null;
        boolean isPrivate = message.getChannel().isPrivate();
        if (!isPrivate) guild = message.getGuild();
        String text = message.getContent();
        Matcher matcher = COMMAND_PATTERN.matcher(text);
        if (matcher.matches()) {
            String baseCommand = matcher.group(1).toLowerCase();
            Optional<Command> command = registeredCommands.stream()
                    .filter(com -> com.getName().equalsIgnoreCase(baseCommand) || (com.getAliases() != null && com.getAliases().contains(baseCommand)))
                    .findAny();
            if (command.isPresent()) {
                System.out.println("@" + message.getAuthor().getName() + " issued \"" + text + "\" in " +
                        (isPrivate ? ("@" + message.getAuthor().getName()) : guild.getName()));

                String args = matcher.group(2);
                String[] argsArr = args.isEmpty() ? new String[0] : args.split(" ");
                List<Long> roleIDs = message.getAuthor().getRolesForGuild(guild).stream().map(role -> role.getLongID()).collect(Collectors.toList());
                command.get().execute(this, client, argsArr, guild, roleIDs, message, isPrivate);
            }
        } else if (!message.getChannel().isPrivate()){
            censorMessages(message);
        }
    }

    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) {
        System.out.println("Logged in.");

        startTime = System.currentTimeMillis();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public IDiscordClient getClient() {
        return client;
    }

    public List<String> getBotAdmins() {
        return botAdmins;
    }

    private void registerAllCommands() {
        new Reflections("cback.commands").getSubTypesOf(Command.class).forEach(commandImpl -> {
            try {
                Command command = commandImpl.newInstance();
                Optional<Command> existingCommand = registeredCommands.stream().filter(cmd -> cmd.getName().equalsIgnoreCase(command.getName())).findAny();
                if (!existingCommand.isPresent()) {
                    registeredCommands.add(command);
                    System.out.println("Registered command: " + command.getName());
                } else {
                    System.out.println("Attempted to register two commands with the same name: " + existingCommand.get().getName());
                    System.out.println("Existing: " + existingCommand.get().getClass().getName());
                    System.out.println("Attempted: " + commandImpl.getName());
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public String getUptime() {
        long totalSeconds = (System.currentTimeMillis() - startTime) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = (totalSeconds / 3600);
        return (hours < 10 ? "0" + hours : hours) + "h " + (minutes < 10 ? "0" + minutes : minutes) + "m " + (seconds < 10 ? "0" + seconds : seconds) + "s";
    }

    public static TheOfficialBot getInstance() {
        return instance;
    }

    //checks for dirty words
    public void censorMessages(IMessage message) {
        if (!message.getChannel().isPrivate()) {
            List<String> bannedWords = TheOfficialBot.getInstance().getConfigManager().getConfigArray("bannedWords");
            String content = message.getFormattedContent().toLowerCase();
            Boolean tripped = false;
            for (String word : bannedWords) {
                if (content.matches(".*\\b" + word + "\\b.*") || content.matches(".*\\b" + word + "s\\b.*")) {
                    tripped = true;
                    break;
                }
            }
            if (tripped) {
                message.getChannel().setTypingStatus(true);
                IUser author = message.getAuthor();

                EmbedBuilder bld = new EmbedBuilder();
                bld
                        .withAuthorIcon(author.getAvatarURL())
                        .withAuthorName(Util.getTag(author))
                        .withDesc(message.getFormattedContent())
                        .withTimestamp(System.currentTimeMillis())
                        .withFooterText("Auto-deleted from #" + message.getChannel().getName());

                Util.sendEmbed(message.getGuild().getChannelByID(Long.parseLong("266651712826114048")), bld.withColor(161, 61, 61).build());
                Util.sendPrivateMessage(author, "Your message has been automatically removed for a banned word or something");

                message.delete();
                message.getChannel().setTypingStatus(false);
            }
        }
    }

}