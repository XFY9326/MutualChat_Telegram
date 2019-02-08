package plugin.xfy9326.mutualchat.telegram;

import java.util.Locale;
import java.util.ResourceBundle;

class I18n {
    private ResourceBundle resourceBundle;

    I18n(Locale locale) {
        resourceBundle = ResourceBundle.getBundle("i18n.lang", locale);
    }

    String get(String key) {
        return resourceBundle.getString(key);
    }
}
