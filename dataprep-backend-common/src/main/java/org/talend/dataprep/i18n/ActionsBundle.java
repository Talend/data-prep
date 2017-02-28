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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.parameters.Parameter;

/**
 * Non-spring accessor to actions resources bundle.
 */
public class ActionsBundle implements MessagesBundle {

    public static final ActionsBundle INSTANCE = new ActionsBundle();

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionsBundle.class);

    private static final String ACTIONS_MESSAGES = "actions_messages";

    private static final String BUNDLE_NAME = "org.talend.dataprep.i18n." + ACTIONS_MESSAGES;

    private static final String ACTION_PREFIX = "action.";

    private static final String DESCRIPTION_SUFFIX = ".desc";

    private static final String URL_SUFFIX = ".url";

    private static final String LABEL_SUFFIX = ".label";

    private static final String PARAMETER_PREFIX = "parameter.";

    private static final String CHOICE_PREFIX = "choice.";

    /**
     * Represents the fallBackKey used to map the default resource bundle since a concurrentHashMap does not map a null key.
     */
    private final Class fallBackKey;

    private final Map<Class, ResourceBundle> actionToResourceBundle = new ConcurrentHashMap<>();

    private ActionsBundle() {
        fallBackKey = this.getClass();
        actionToResourceBundle.put(fallBackKey, ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH));
    }

    /**
     * Link all <code>parameters</code> to the <code>parent</code>: when looking for parameters translation, bundle
     * will use <code>parent</code> to find resource bundle.
     * @param parameters The {@link Parameter parameters} to attach to <code>parent</code>.
     * @param parent An object to be used in resource bundle search.
     * @return A list of {@link Parameter parameters} that will use <code>parent</code> to look for message keys.
     * @see Parameter#attach(Object)
     */
    public static List<Parameter> attachToAction(List<Parameter> parameters, Object parent) {
        return parameters.stream().map(p -> p.attach(parent)).collect(Collectors.toList());
    }

    /**
     * Format the message template with provided arguments
     * @param template The string template
     * @param args The arguments
     */
    private String formatMessage(final String template, final Object... args) {
        final MessageFormat messageFormat = new MessageFormat(template);
        return messageFormat.format(args);
    }

    /**
     * Get the message from bundle or fallback bundle.
     * If message is not present, null is returned
     */
    private String getOptionalMessage(Object action, Locale locale, String code, Object... args) {
        final ResourceBundle bundle = findBundle(action, locale);
        // We can put some cache here if default internal caching it is not enough
        if (bundle.containsKey(code)) {
            return formatMessage(bundle.getString(code), args);
        } else if(actionToResourceBundle.get(fallBackKey).containsKey(code)) {
            return formatMessage(actionToResourceBundle.get(fallBackKey).getString(code), args);
        }
        return null;
    }

    /**
     * Get the message from bundle or fallback bundle.
     * Ig message is not present, a TalendRuntimeException is thrown
     */
    private String getMandatoryMessage(Object action, Locale locale, String code, Object... args) {
        final String message = getOptionalMessage(action, locale, code, args);
        if (message == null) {
            LOGGER.info("Unable to find key '{}' using context '{}'.", code, action);
            throw new TalendRuntimeException(BaseErrorCodes.MISSING_I18N);
        }
        return message;
    }

    private ResourceBundle findBundle(Object action, Locale locale) {
        if (action == null) {
            return actionToResourceBundle.get(fallBackKey);
        }
        if (actionToResourceBundle.containsKey(action.getClass())) {
            final ResourceBundle resourceBundle = actionToResourceBundle.get(action.getClass());
            LOGGER.debug("Cache hit for action '{}': '{}'", action, resourceBundle);
            return resourceBundle;
        }
        // Lookup for resource bundle in package hierarchy
        final Package actionPackage = action.getClass().getPackage();
        String currentPackageName = actionPackage.getName();
        ResourceBundle bundle = null;
        while (currentPackageName.contains(".")) {
            try {
                bundle = ResourceBundle.getBundle(currentPackageName + '.' + ACTIONS_MESSAGES, locale);
                break; // Found, exit lookup
            } catch (MissingResourceException e) {
                LOGGER.debug("No action resource bundle found for action '{}' at '{}'", action, currentPackageName, e);
            }
            currentPackageName = StringUtils.substringBeforeLast(currentPackageName, ".");
        }
        if (bundle == null) {
            LOGGER.debug("Choose default action resource bundle for action '{}'", action);
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        }
        actionToResourceBundle.putIfAbsent(action.getClass(), bundle);
        return bundle;
    }

    /**
     * Fetches action label at {@code action.<action_name>.label} in the dataprep actions resource bundle. If message does not
     * exist, code will lookup in {@link #fallBackKey} resource bundle (i.e. Data Prep one) for message.
     */
    public String actionLabel(Object action, Locale locale, String actionName, Object... values) {
        final String actionLabelKey = ACTION_PREFIX + actionName + LABEL_SUFFIX;
        return getMandatoryMessage(action, locale, actionLabelKey, values);
    }

    /**
     * Fetches action description at {@code action.<action_name>.desc} in the dataprep actions resource bundle. If message does
     * not exist, code will lookup in {@link #fallBackKey} resource bundle (i.e. Data Prep one) for message.
     */
    public String actionDescription(Object action, Locale locale, String actionName, Object... values) {
        final String actionDescriptionKey = ACTION_PREFIX + actionName + DESCRIPTION_SUFFIX;
        return getMandatoryMessage(action, locale, actionDescriptionKey, values);
    }

    /**
     * Fetches action doc url at {@code action.<action_name>.url} in the dataprep actions resource bundle.
     * If there is no doc for this action, an empty string is returned.
     */
    public String actionDocUrl(Object action, Locale locale, String actionName) {
        final String actionDocUrlKey = ACTION_PREFIX + actionName + URL_SUFFIX;
        final String docUrl = getOptionalMessage(action, locale, actionDocUrlKey);

        if (docUrl == null) {
            return StringUtils.EMPTY;
        }
        return docUrl;
    }

    /**
     * Fetches parameter label at {@code parameter.<parameter_name>.label} in the dataprep actions resource bundle. If message
     * does not exist, code will lookup in {@link #fallBackKey} resource bundle (i.e. Data Prep one) for message.
     */
    public String parameterLabel(Object action, Locale locale, String parameterName, Object... values) {
        final String parameterLabelKey = PARAMETER_PREFIX + parameterName + LABEL_SUFFIX;
        return getMandatoryMessage(action, locale, parameterLabelKey, values);
    }

    /**
     * Fetches parameter description at {@code parameter.<parameter_name>.desc} in the dataprep actions resource bundle. If
     * message does not exist, code will lookup in {@link #fallBackKey} resource bundle (i.e. Data Prep one) for message.
     */
    public String parameterDescription(Object action, Locale locale, String parameterName, Object... values) {
        final String parameterDescriptionKey = PARAMETER_PREFIX + parameterName + DESCRIPTION_SUFFIX;
        return getMandatoryMessage(action, locale, parameterDescriptionKey, values);
    }

    /**
     * Fetches choice at {@code choice.<choice_name>} in the dataprep actions resource bundle. If message does not exist, code
     * will lookup in {@link #fallBackKey} resource bundle (i.e. Data Prep one) for message.
     */
    public String choice(Object action, Locale locale, String choiceName, Object... values) {
        final String choiceKey = CHOICE_PREFIX + choiceName;
        return getMandatoryMessage(action, locale, choiceKey, values);
    }

    @Override
    public String getString(Locale locale, String code) {
        return getMandatoryMessage(null, locale, code);
    }

    @Override
    public String getString(Locale locale, String code, String defaultMessage) {
        return getMandatoryMessage(null, locale, code);
    }

    @Override
    public String getString(Locale locale, String code, Object... args) {
        return getMandatoryMessage(fallBackKey, locale, code, args);
    }

}
