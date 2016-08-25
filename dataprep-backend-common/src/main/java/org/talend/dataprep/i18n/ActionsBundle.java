// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;

/**
 * Non-spring accessor to actions resources bundle.
 */
public class ActionsBundle implements MessagesBundle {

    public static final ActionsBundle INSTANCE = new ActionsBundle();

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionsBundle.class);

    private static final String BUNDLE_NAME = "org.talend.dataprep.i18n.actions_messages";

    private ActionsBundle() {
    }

    private String getMessage(Locale locale, String code, Object... args) {
        // We can put some cache here if default internal caching it is not enough
        MessageFormat messageFormat;
        try {
            messageFormat = new MessageFormat(ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(code));
        } catch (MissingResourceException e) {
            LOGGER.info("Unable to find key '{}'.", code, e);
            throw new TalendRuntimeException(BaseErrorCodes.MISSING_I18N, e);
        }
        return messageFormat.format(args);
    }

    /**
     * Fetches action label at {@code action.<action_name>.label} in the dataprep actions resource bundle.
     */
    public String actionLabel(Locale locale, String actionName, Object... values) {
        final String actionLabelKey = ActionMessagesDelegate.getActionLabelKey(actionName);
        return getMessage(locale, actionLabelKey, values);
    }

    /**
     * Fetches action description at {@code action.<action_name>.desc} in the dataprep actions resource bundle.
     */
    public String actionDescription(Locale locale, String actionName, Object... values) {
        final String actionDescriptionKey = ActionMessagesDelegate.getActionDescriptionKey(actionName);
        return getMessage(locale, actionDescriptionKey, values);
    }

    /**
     * Fetches parameter label at {@code parameter.<parameter_name>.label} in the dataprep actions resource bundle.
     */
    public String parameterLabel(Locale locale, String parameterName, Object... values) {
        final String parameterLabelKey = ActionMessagesDelegate.getParameterLabelKey(parameterName);
        return getMessage(locale, parameterLabelKey, values);
    }

    /**
     * Fetches parameter description at {@code parameter.<parameter_name>.desc} in the dataprep actions resource bundle.
     */
    public String parameterDescription(Locale locale, String parameterName, Object... values) {
        final String parameterDescriptionKey = ActionMessagesDelegate.getParameterDescriptionKey(parameterName);
        return getMessage(locale, parameterDescriptionKey, values);
    }

    /**
     * Fetches choice at {@code choice.<choice_name>} in the dataprep actions resource bundle.
     */
    public String choice(Locale locale, String choiceName, Object... values) {
        final String choiceKey = ActionMessagesDelegate.getChoiceKey(choiceName);
        try {
            return getMessage(locale, choiceKey, values);
        } catch (Exception e) {
            LOGGER.debug("Unable to find choice key '{}' for choice '{}'", choiceKey, choiceName);
            return choiceName;
        }
    }

    @Override
    public String getString(Locale locale, String code) {
        return getMessage(locale, code);
    }

    @Override
    public String getString(Locale locale, String code, String defaultMessage) {
        final String message = getMessage(locale, code);
        return message == null ? defaultMessage : message;
    }

    @Override
    public String getString(Locale locale, String code, Object... args) {
        return getMessage(locale, code, args);
    }
}
