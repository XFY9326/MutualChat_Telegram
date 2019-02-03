package plugin.xfy9326.mutualchat.telegram;

import org.telegram.telegrambots.bots.DefaultBotOptions;

class Config {
    static final int MIN_CONFIG_VERSION = 3;

    static final String PLAYER = "@p";
    static final String ADDRESS = "@address";
    static final String MSG = "@msg";
    static final String POSITION_WORLD = "@world";
    static final String POSITION_X = "@px";
    static final String POSITION_Y = "@py";
    static final String POSITION_Z = "@pz";

    static final String TELEGRAM_TOKEN = "telegram.token";
    static final String TELEGRAM_BOT_NAME = "telegram.bot_user_name";
    static final String TELEGRAM_GROUP_CHAT_ID = "telegram.chat_group_id";
    static final String PROXY_HOST = "telegram.proxy.host";
    static final String PROXY_PORT = "telegram.proxy.port";
    static final String PROXY_TYPE = "telegram.proxy.type";

    static final String SHOW_PLAYER_JOIN_AND_QUIT_MSG = "MutualChat.msg.player_join_and_quit";
    static final String PLAYER_JOIN_MSG = "MutualChat.msg.player_join_msg";
    static final String PLAYER_QUIT_MSG = "MutualChat.msg.player_quit_msg";
    static final String MSG_TELEGRAM_SHOW = "MutualChat.msg.telegram_show_msg";
    static final String MSG_GAME_SHOW = "MutualChat.msg.game_show_msg";
    static final String PLAYER_SEND_POSITION_MSG = "MutualChat.msg.player_send_position_msg";
    static final String CONFIG_VERSION = "config_version";

    static final String SEND_MSG_ONLY_WHEN_PLAYER_ONLINE = "MutualChat.send_msg_only_when_player_online";

    static final boolean DEFAULT_SHOW_PLAYER_JOIN_AND_QUIT_MSG = true;
    static final boolean DEFAULT_SEND_MSG_ONLY_WHEN_PLAYER_ONLINE = false;
    static final String DEFAULT_PLAYER_JOIN_MSG = "@p joined game [@address]";
    static final String DEFAULT_PLAYER_QUIT_MSG = "@p quit game";
    static final String DEFAULT_MSG_TELEGRAM_SHOW = "[@p] @msg";
    static final String DEFAULT_MSG_GAME_SHOW = "<@p> @msg";
    static final String DEFAULT_PLAYER_SEND_POSITION_MSG = "World:@world X:@px Y:@py Z:@pz";

    static final String COMMAND_SEND_POSITION = "sendposition";

    static final String NO_PROXY = "NO_PROXY";
    static final String NULL = "Null";
    private static final String HTTP = "HTTP";
    private static final String SOCKS4 = "SOCK4";
    private static final String SOCKS5 = "SOCKS5";

    static DefaultBotOptions.ProxyType getProxyType(String type) {
        if (type == null) {
            return DefaultBotOptions.ProxyType.NO_PROXY;
        } else {
            switch (type) {
                case HTTP:
                    return DefaultBotOptions.ProxyType.HTTP;
                case SOCKS4:
                    return DefaultBotOptions.ProxyType.SOCKS4;
                case SOCKS5:
                    return DefaultBotOptions.ProxyType.SOCKS5;
                case NO_PROXY:
                    return DefaultBotOptions.ProxyType.NO_PROXY;
                default:
                    return DefaultBotOptions.ProxyType.NO_PROXY;
            }
        }
    }
}
