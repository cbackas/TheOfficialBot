package cback;

import cback.commands.Command;
import cback.events.ChannelChange;
import cback.events.MemberChange;
import cback.events.MessageChange;
import cback.events.ReactionChange;
import com.vdurmont.emoji.EmojiManager;
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

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OfficialBot {
    private static OfficialBot instance;
    private static IDiscordClient client;

    private static ConfigManager configManager;
    private Scheduler scheduler;

    public static List<Command> registeredCommands = new ArrayList<>();

    public static ArrayList<Long> messageCache = new ArrayList<>();
    public static List<Long> alreadyCalled = new ArrayList<>();

    static private String prefix = "?";
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^\\?([^\\s]+) ?(.*)", Pattern.CASE_INSENSITIVE);
    public List<String> prefixes = new ArrayList<>();

    public static final long CBACK_USR_ID = 73416411443113984L;
    public static final long HOME_GUILD_ID = 266649217538195457L;

    public static final long INFO_CAT_ID = 355925879853023232L;

    public static final long ANNOUNCEMENT_CH_ID = 318098998047277057L;
    public static final long GENERAL_CH_ID = 266649217538195457L;
    public static final long SERVERLOG_CH_ID = 281021113440534528L;
    public static final long SERVERCHANGELOG_CH_ID = 363138434153578496L;
    public static final long MESSAGELOG_CH_ID = 347078737726275605L;
    public static final long STARBOARD_CH_ID = 374978760547762176L;
    public static final long STAFF_CH_ID = 266651712826114048L;
    public static final long ADMIN_CH_ID = 285470408709373954L;
    public static final long DEV_CH_ID = 277587347443286016L;

    public static final long ERRORLOG_CH_ID = 346104666796589056L;
    public static final long BOTLOG_CH_ID = 346483682376286208L;
    public static final long BOTPM_CH_ID = 346104720903110656L;

    private long startTime;

    public static void main(String[] args) {
        new OfficialBot();
    }

    public OfficialBot() {

        instance = this;
        registerAllCommands();

        //instantiate config manager first as connect() relies on tokens
        configManager = new ConfigManager(this);
        prefixes.add(OfficialBot.getPrefix());
        prefixes.add("t!");
        prefixes.add("!");
        prefixes.add("!g");
        prefixes.add("--");
        prefixes.add(".");

        connect();
        client.getDispatcher().registerListener(this);
        client.getDispatcher().registerListener(new ChannelChange(this));
        client.getDispatcher().registerListener(new MemberChange(this));
        client.getDispatcher().registerListener(new MessageChange(this));
        client.getDispatcher().registerListener(new ReactionChange(this));

        scheduler = new Scheduler(this);
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

    /*
     * Message Central Choo Choo
     */
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
                Command cCommand = command.get();

                if (cCommand.getDescription() != null || message.getAuthor().getLongID() == CBACK_USR_ID) {
                    System.out.println("@" + message.getAuthor().getName() + " issued \"" + text + "\" in " +
                            (isPrivate ? ("@" + message.getAuthor().getName()) : guild.getName()));

                    String args = matcher.group(2);
                    String[] argsArr = args.isEmpty() ? new String[0] : args.split(" ");

                    List<Long> roleIDs = message.getAuthor().getRolesForGuild(guild).stream().map(role -> role.getLongID()).collect(Collectors.toList());

                    IUser author = message.getAuthor();
                    String content = message.getContent();

                    /**
                     * If user has permission to run the command: Command executes and botlogs
                     */
                    if (cCommand.getPermissions() == null || !Collections.disjoint(roleIDs, cCommand.getPermissions())) {
                        Util.botLog(message);
                        cCommand.execute(message, content, argsArr, author, guild, roleIDs, isPrivate, client, this);
                    } else {
                        Util.simpleEmbed(message.getChannel(), "You don't have permission to perform this command.");
                    }
                }
            }
            /**
             * Forwards the random stuff people PM to the bot - to me
             */
        } else if (message.getChannel().isPrivate()) {
            EmbedBuilder bld = new EmbedBuilder()
                    .withColor(getBotColor())
                    .withTimestamp(System.currentTimeMillis())
                    .withAuthorName(message.getAuthor().getName() + '#' + message.getAuthor().getDiscriminator())
                    .withAuthorIcon(message.getAuthor().getAvatarURL())
                    .withDesc(message.getContent());

            for (IMessage.Attachment a : message.getAttachments()) {
                bld.withImage(a.getUrl());
            }

            Util.sendEmbed(client.getChannelByID(BOTPM_CH_ID), bld.build());
        } else {
            censorMessages(message);

            /**
             * Kaya's reaction request
             */
            if (message.getMentions().contains(guild.getUserByID(137294678721691648l)) || message.getContent().toLowerCase().contains("kaya")) {
                if (message.getAuthor().getStringID().equals("110211413531705344")) {
                    message.addReaction(EmojiManager.getByUnicode("\uD83C\uDFF3"));//white flag
                }
                if (message.getAuthor().getStringID().equals("275425737220292608")) {
                    message.addReaction(EmojiManager.getByUnicode("\uD83C\uDF69"));//doughnut
                }
            }
        }
    }

    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) {
        System.out.println("Logged in.");
        client = event.getClient();

        //Set status
        client.changePlayingText("Type " + prefix + "help");

        startTime = System.currentTimeMillis();
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static OfficialBot getInstance() {
        return instance;
    }

    public static IDiscordClient getClient() {
        return client;
    }

    public static IGuild getHomeGuild() {
        IGuild homeGuild;homeGuild = getClient().getGuildByID(HOME_GUILD_ID);
        return homeGuild;
    }

    public static String getPrefix() {
        return prefix;
    }

    public static Color getBotColor() {
        return Color.decode("#" + configManager.getConfigValue("bot_color"));
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
            } catch (InstantiationException | IllegalAccessException e) {
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

    //checks for dirty words
    public void censorMessages(IMessage message) {
        boolean homeGuild = message.getGuild().getStringID().equals(getHomeGuild().getStringID());
        boolean staffChannel = message.getChannel().getCategory().getStringID().equals("355911822886567936");
        boolean staffMember = message.getAuthor().hasRole(message.getClient().getRoleByID(OfficialRoles.STAFF.id));
        if (homeGuild && !staffChannel && !staffMember) {
            List<String> bannedWords = OfficialBot.getInstance().getConfigManager().getConfigArray("bannedWords");
            String content = message.getFormattedContent().toLowerCase();

            String word = "";
            Boolean tripped = false;
            for (String w : bannedWords) {
                if (content.matches("\\n?.*\\b\\n?" + w + "\\n?\\b.*\\n?.*") || content.matches("\\n?.*\\b\\n?" + w + "s\\n?\\b.*\\n?.*")) {
                    tripped = true;
                    word = w;
                    break;
                }
            }
            if (tripped) {

                IUser author = message.getAuthor();

                EmbedBuilder bld = new EmbedBuilder();
                bld
                        .withAuthorIcon(author.getAvatarURL())
                        .withAuthorName(Util.getTag(author))
                        .withDesc(message.getFormattedContent())
                        .withTimestamp(System.currentTimeMillis())
                        .withFooterText("Auto-deleted from #" + message.getChannel().getName());

                Util.sendEmbed(message.getGuild().getChannelByID(MESSAGELOG_CH_ID), bld.withColor(getBotColor()).build());

                StringBuilder sBld = new StringBuilder().append("Your message has been automatically removed for containing a banned word. If this is an error, message a staff member.");
                if (!word.isEmpty()) {
                    sBld
                            .append("\n\n")
                            .append(word);
                }
                Util.sendPrivateEmbed(author, sBld.toString());

                messageCache.add(message.getLongID());
                Util.deleteMessage(message);
            }
        }
    }

}