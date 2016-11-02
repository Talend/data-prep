package org.talend.dataprep.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.talend.dataprep.i18n.ActionMessagesDelegate.*;

/**
 * Non-spring accessor to actions resources bundle.
 */
public class ActionsBundle {

    private static final ActionsBundle INSTANCE = new ActionsBundle();

    public static final String BUNDLE_NAME = "org.talend.dataprep.i18n.actions_messages";

    private String getMessage(Locale locale, String code, Object... args) {
        // We can put some cache here if default internal caching it is not enough
        MessageFormat messageFormat = new MessageFormat(ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(code));
        return messageFormat.format(args);
    }

    /**
     * Fetches action label at {@code action.<action_name>.label} in the dataprep actions resource bundle.
     */
    public static String actionLabel(Locale locale, String actionName, Object... values) {
        return INSTANCE.getMessage(locale, getActionLabelKey(actionName), values);
    }

    /**
     * Fetches action description at {@code action.<action_name>.desc} in the dataprep actions resource bundle.
     */
    public static String actionDescription(Locale locale, String actionName, Object... values) {
        return INSTANCE.getMessage(locale, getActionDescriptionKey(actionName), values);
    }

    /**
     * Fetches parameter label at {@code parameter.<parameter_name>.label} in the dataprep actions resource bundle.
     */
    public static String parameterLabel(Locale locale, String parameterName, Object... values) {
        return INSTANCE.getMessage(locale, getParameterLabelKey(parameterName), values);
    }

    /**
     * Fetches parameter description at {@code parameter.<parameter_name>.desc} in the dataprep actions resource bundle.
     */
    public static String parameterDescription(Locale locale, String parameterName, Object... values) {
        return INSTANCE.getMessage(locale, getParameterDescriptionKey(parameterName), values);
    }

    /**
     * Fetches choice at {@code choice.<choice_name>} in the dataprep actions resource bundle.
     */
    public static String choice(Locale locale, String choiceName, Object... values) {
        return INSTANCE.getMessage(locale, getChoiceKey(choiceName), values);
    }

}
