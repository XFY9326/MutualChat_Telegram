package plugin.xfy9326.mutualchat.telegram;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public final class MainPlugin extends JavaPlugin implements Listener {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.00");
    private static boolean apiRegistered = false;
    private Bot bot = null;
    private BotSession botSession;
    private I18n i18n;

    public MainPlugin() {
        saveDefaultConfig();
        ApiContextInitializer.init();
    }

    @Override
    public void onLoad() {
        setI18n();
        if (!apiRegistered && checkConfigVersion()) {
            String token = getConfig().getString(Config.TELEGRAM_TOKEN);
            String botName = getConfig().getString(Config.TELEGRAM_BOT_NAME);
            List<String> chatId = getConfig().getStringList(Config.TELEGRAM_GROUP_CHAT_ID);

            if (token == null || botName == null || chatId == null || Config.NULL.equalsIgnoreCase(token) || Config.NULL.equalsIgnoreCase(botName)) {
                getLogger().warning(i18n.get(Config.LANG_CONFIG_ERROR));
            } else {
                TelegramBotsApi botsApi = new TelegramBotsApi();

                DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

                String proxyType = getConfig().getString(Config.PROXY_TYPE);
                if (!Config.NO_PROXY.equals(proxyType)) {
                    botOptions.setProxyHost(getConfig().getString(Config.PROXY_HOST));
                    botOptions.setProxyPort(getConfig().getInt(Config.PROXY_PORT));
                    botOptions.setProxyType(Config.getProxyType(proxyType));
                }
                bot = new Bot(botName, token, chatId, botOptions);
                bot.setMsgListener(this::sendMsgToServer);
                try {
                    botSession = botsApi.registerBot(bot);
                    apiRegistered = true;
                } catch (TelegramApiException e) {
                    getLogger().warning(e.getMessage());
                }
            }
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (bot != null) {
            String token = getConfig().getString(Config.TELEGRAM_TOKEN);
            String botName = getConfig().getString(Config.TELEGRAM_BOT_NAME);
            List<String> chatId = getConfig().getStringList(Config.TELEGRAM_GROUP_CHAT_ID);

            bot.updateBotConfig(botName, token, chatId);
        }
        if (botSession != null) {
            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

            String proxyType = getConfig().getString(Config.PROXY_TYPE);
            if (!Config.NO_PROXY.equals(proxyType)) {
                botOptions.setProxyHost(getConfig().getString(Config.PROXY_HOST));
                botOptions.setProxyPort(getConfig().getInt(Config.PROXY_PORT));
                botOptions.setProxyType(Config.getProxyType(proxyType));
            }

            botSession.setOptions(botOptions);
        }
    }

    @Override
    public void onEnable() {
        if (apiRegistered) {
            Bukkit.getPluginManager().registerEvents(this, this);
        } else {
            getLogger().warning(i18n.get(Config.LANG_BOT_UNREGISTERED));
        }
    }

    @Override
    public void onDisable() {
        if (apiRegistered && botSession != null) {
            getLogger().info(i18n.get(Config.LANG_IGNORE_BOTSESSION_ERROR));
            new Thread(() -> {
                if (botSession.isRunning()) {
                    botSession.stop();
                }
            }).start();
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent chatEvent) {
        sendMsgToBot(chatEvent.getPlayer().getDisplayName(), chatEvent.getMessage());
    }

    @EventHandler
    public void onJoinServer(PlayerJoinEvent joinEvent) {
        if (getConfig().getBoolean(Config.SHOW_PLAYER_JOIN_AND_QUIT_MSG, Config.DEFAULT_SHOW_PLAYER_JOIN_AND_QUIT_MSG)) {
            sendMsgToBot(null, getConfig().getString(Config.PLAYER_JOIN_MSG, Config.DEFAULT_PLAYER_JOIN_MSG)
                    .replace(Config.PLAYER, joinEvent.getPlayer().getDisplayName())
                    .replace(Config.ADDRESS, joinEvent.getPlayer().getAddress().getHostString() + ":" + joinEvent.getPlayer().getAddress().getPort()));
        }
    }

    @EventHandler
    public void onQuitServer(PlayerQuitEvent quitEvent) {
        if (getConfig().getBoolean(Config.SHOW_PLAYER_JOIN_AND_QUIT_MSG, Config.DEFAULT_SHOW_PLAYER_JOIN_AND_QUIT_MSG)) {
            sendMsgToBot(null, getConfig().getString(Config.PLAYER_QUIT_MSG, Config.DEFAULT_PLAYER_QUIT_MSG)
                    .replace(Config.PLAYER, quitEvent.getPlayer().getDisplayName()));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (Config.COMMAND_SEND_POSITION.equals(command.getName())) {
                String x = decimalFormat.format(((Player) sender).getLocation().getX());
                String y = decimalFormat.format(((Player) sender).getLocation().getY());
                String z = decimalFormat.format(((Player) sender).getLocation().getZ());
                String world = ((Player) sender).getWorld().getName();
                String sendMsg = getConfig().getString(Config.PLAYER_SEND_POSITION_MSG, Config.DEFAULT_PLAYER_SEND_POSITION_MSG)
                        .replace(Config.POSITION_WORLD, world)
                        .replace(Config.POSITION_X, x)
                        .replace(Config.POSITION_Y, y)
                        .replace(Config.POSITION_Z, z);
                sendMsgToBot(((Player) sender).getDisplayName(), sendMsg);
                sendMsgToServer(((Player) sender).getDisplayName(), sendMsg);
                return true;
            }
        } else {
            sender.sendMessage(i18n.get(Config.LANG_ENTITY_ERROR));
        }
        return false;
    }

    private void setI18n() {
        String local = getConfig().getString(Config.I18N);
        Locale locale = Config.getLocale(local);
        i18n = new I18n(locale);
    }

    private void sendMsgToServer(String player, String msg) {
        if (apiRegistered) {
            if (!getConfig().getBoolean(Config.SEND_MSG_ONLY_WHEN_PLAYER_ONLINE, Config.DEFAULT_SEND_MSG_ONLY_WHEN_PLAYER_ONLINE)
                    || getServer().getOnlinePlayers().size() > 0) {
                getServer().broadcastMessage(getConfig().getString(Config.MSG_GAME_SHOW, Config.DEFAULT_MSG_GAME_SHOW)
                        .replace(Config.PLAYER, player)
                        .replace(Config.MSG, msg));
            }
        }
    }

    private void sendMsgToBot(final String player, final String msg) {
        if (apiRegistered) {
            if (bot != null) {
                Bukkit.getScheduler().runTask(this, () -> {
                    if (player == null) {
                        bot.sendMsg(msg, msg1 -> getLogger().info(msg1));
                    } else {
                        bot.sendMsg(getConfig().getString(Config.MSG_TELEGRAM_SHOW, Config.DEFAULT_MSG_TELEGRAM_SHOW)
                                .replace(Config.PLAYER, player)
                                .replace(Config.MSG, msg), msg1 -> getLogger().info(msg1));
                    }
                });
            } else {
                getLogger().warning(i18n.get(Config.LANG_BOT_MSG_SEND_ERROR));
            }
        }
    }

    private boolean checkConfigVersion() {
        int configVersion = getConfig().getInt(Config.CONFIG_VERSION, 0);
        if (configVersion < Config.MIN_CONFIG_VERSION) {
            getLogger().warning(i18n.get(Config.LANG_CONFIG_OUTDATED));
            return false;
        }
        return true;
    }
}
