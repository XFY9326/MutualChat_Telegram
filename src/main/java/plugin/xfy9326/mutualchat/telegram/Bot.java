package plugin.xfy9326.mutualchat.telegram;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

class Bot extends TelegramLongPollingBot {
    private String TOKEN;
    private String BOT_NAME;
    private List<String> chatIdArr;
    private MsgListener msgListener = null;

    Bot(String botName, String token, List<String> chatIdArr, DefaultBotOptions botOptions) {
        super(botOptions);
        this.TOKEN = token;
        this.BOT_NAME = botName;
        this.chatIdArr = chatIdArr;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (msgListener != null && chatIdArr.contains(update.getMessage().getChatId().toString()) && update.hasMessage()) {
            String name = update.getMessage().getFrom().getUserName();
            if (name == null) {
                name = update.getMessage().getFrom().getFirstName();
            }
            if (update.getMessage().hasText()) {
                msgListener.onMsgReceived(name, update.getMessage().getText());
            }
        }
    }

    @Override
    public String getBotUsername() {
        return this.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return this.TOKEN;
    }

    synchronized void updateBotConfig(String botName, String token, List<String> chatIdArr) {
        this.TOKEN = token;
        this.BOT_NAME = botName;
        this.chatIdArr = chatIdArr;
    }

    void setMsgListener(MsgListener msgListener) {
        this.msgListener = msgListener;
    }

    void sendMsg(String msg, final OnErrorListener onErrorListener) {
        for (String id : chatIdArr) {
            SendMessage message = new SendMessage()
                    .setChatId(id)
                    .setText(msg);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                if (onErrorListener != null) {
                    onErrorListener.onError(e.getMessage());
                }
            }
        }
    }

    interface OnErrorListener {
        void onError(String msg);
    }

    interface MsgListener {
        void onMsgReceived(String userName, String msg);
    }
}
