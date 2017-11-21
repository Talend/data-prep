// ============================================================================
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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;

/**
 * Non-spring accessor to actions resources bundle.
 */
public class ActionsBundle implements MessagesBundle {

    private static final ActionsBundle INSTANCE = new ActionsBundle();

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionsBundle.class);

    private static final String ACTIONS_MESSAGES = "actions_messages";

    private static final String BUNDLE_NAME = "org.talend.dataprep.i18n." + ACTIONS_MESSAGES;

    private static final String ACTION_PREFIX = "action.";

    private static final String DESCRIPTION_SUFFIX = ".desc";

    private static final String URL_SUFFIX = ".url";

    private static final String URL_PARAMETERS_SUFFIX = ".url_parameters";

    private static final String LABEL_SUFFIX = ".label";

    private static final String PARAMETER_PREFIX = "parameter.";

    private static final String CATEGORY_PREFIX = "category.";

    private static final String CHOICE_PREFIX = "choice.";

    /**
     * Represents the fallBackKey used to map the default resource bundle since a concurrentHashMap does not map a null key.
     */
    private final String fallBackKey;

    private final Map<String, ResourceBundle> actionToResourceBundle = new ConcurrentHashMap<>();

    /** Base URL of the documentation portal. */
    // Documentation URL is not thread safe. It is acceptable while it is only changed at the application startup.
    // If more changes is to happen, there should be a thread safety mechanism
    private String documentationUrlBase;

    private ActionsBundle() {
        fallBackKey = ActionsBundle.generateBundleKey(this.getClass());
        actionToResourceBundle.put(fallBackKey, ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()));
    }

    private static String generateBundleKey(Class clazz, Locale locale) {
        if (Objects.isNull(locale)) {
            locale = Locale.getDefault();
        }
        return clazz.getName() + "_" + locale.getLanguage();
    }

    private static String generateBundleKey(Class clazz) {
        return ActionsBundle.generateBundleKey(clazz, null);
    }

    /**
     * Set global Documentation portal URL.
     *
     * <p>
     * Actions can specify the documentation URL parameters with the {@link #URL_PARAMETERS_SUFFIX} suffix in the
     * {@code actions_messages.properties} file.
     * </p>
     */
    public static void setGlobalDocumentationUrlBase(String documentationUrlBase) {
        INSTANCE.setDocumentationUrlBase(documentationUrlBase);
    }

    /**
     * Get global Documentation portal URL.
     * <p>
     * Actions can specify the documentation URL parameters with the {@link #URL_PARAMETERS_SUFFIX} suffix in the
     * {@code actions_messages.properties} file.
     * </p>
     */
    public static String getGlobalDocumentationUrlBase() {
        return INSTANCE.getDocumentationUrlBase();
    }

    /**
     * Fetches action label at {@code action.<action_name>.label} in the dataprep actions resource bundle. If message does not
     * exist, code will lookup in {@link #fallBackKey} resource bundle (i.e. Data Prep one) for message.
     */
    public static String actionLabel(Object action, Locale locale, String actionName, Object... values) {
        final String actionLabelKey = ACTION_PREFIX + actionName + LABEL_SUFFIX;
        return INSTANCE.getMandatoryMessage(action, locale, actionLabelKey, values);
    }

    /**
     * Fetches action description at {@code action.<action_name>.desc} in the dataprep actions resource bundle. If message does
     * not exist, code will lookup in {@link #fallBackKey} resource bundle (i.e. Data Prep one) for message.
     */
    public static String actionDescription(Object action, Locale locale, String actionName, Object... values) {
        final String actionDescriptionKey = ACTION_PREFIX + actionName + DESCRIPTION_SUFFIX;
        return INSTANCE.getMandatoryMessage(action, locale, actionDescriptionKey, values);
    }

    /**
     * Fetches action doc url at {@code action.<action_name>.url} in the dataprep actions resource bundle.
     * If there is no doc for this action, an empty string is returned.
     */
    public static String actionDocUrl(Object action, Locale locale, String actionName) {
        final String actionDocUrlKey = ACTION_PREFIX + actionName + URL_SUFFIX;
        final String docUrl = INSTANCE.getOptionalMessage(action, locale, actionDocUrlKey);

        if (docUrl == null) {
            final String docParameters = INSTANCE.getOptionalMessage(action, locale, ACTION_PREFIX + actionName + URL_PARAMETERS_SUFFIX);
            if (INSTANCE.documentationUrlBase != null && docParameters != null) {
                return INSTANCE.documentationUrlBase + docParameters;
            }
            return StringUtils.EMPTY;
        }
        return docUrl;
    }

    /**
     * Fetches action label at {@code action.<action_name>.label} in the dataprep actions resource bundle. If message does not
     * exist, code will lookup in {@link #fallBackKey} resource bundle (i.e. Data Prep one) for message.
     */
    public static String categoryName(Object action, Locale locale, String categoryName, Object... values) {
        final String categoryLabelKey = CATEGORY_PREFIX + categoryName + LABEL_SUFFIX;
        return INSTANCE.getMandatoryMessage(action, locale, categoryLabelKey, values);
    }

    /**
     * Fetches parameter label at {@code parameter.<parameter_name>.label} in the dataprep actions resource bundle. If message
     * does not exist, code will lookup in {@link #fallBackKey} resource bundle (i.e. Data Prep one) for message.
     */
    public static String parameterLabel(Object action, Locale locale, String parameterName, Object... values) {
        final String parameterLabelKey = PARAMETER_PREFIX + parameterName + LABEL_SUFFIX;
        return INSTANCE.getMandatoryMessage(action, locale, parameterLabelKey, values);
    }

    /**
     * Fetches parameter description at {@code parameter.<parameter_name>.desc} in the dataprep actions resource bundle. If
     * message does not exist, code will lookup in {@link #fallBackKey} resource bundle (i.e. Data Prep one) for message.
     */
    public static String parameterDescription(Object action, Locale locale, String parameterName, Object... values) {
        final String parameterDescriptionKey = PARAMETER_PREFIX + parameterName + DESCRIPTION_SUFFIX;
        return INSTANCE.getMandatoryMessage(action, locale, parameterDescriptionKey, values);
    }

    /**
     * Fetches choice at {@code choice.<choice_name>} in the dataprep actions resource bundle. If message does not exist, code
     * will lookup in {@link #fallBackKey} resource bundle (i.e. Data Prep one) for message.
     */
    public static String choice(Object action, Locale locale, String choiceName, Object... values) {
        final String choiceKey = CHOICE_PREFIX + choiceName;
        return INSTANCE.getMandatoryMessage(action, locale, choiceKey, values);
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

    /**
     * Format the message template with provided arguments
     *
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
        final String fallbackBundleKey = generateBundleKey(this.getClass());

        // We can put some cache here if default internal caching it is not enough
        if (Objects.nonNull(bundle) && bundle.containsKey(code)) {
            return formatMessage(bundle.getString(code), args);
        } else if (Objects.nonNull(actionToResourceBundle.get(fallbackBundleKey))
                && actionToResourceBundle.get(fallbackBundleKey).containsKey(code)) {
            return formatMessage(actionToResourceBundle.get(fallbackBundleKey).getString(code), args);
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
        String actionBundleKey = ActionsBundle.generateBundleKey(this.getClass(), locale);
        ResourceBundle bundle = actionToResourceBundle.get(actionBundleKey);
        if (Objects.nonNull(action)) {
            actionBundleKey = ActionsBundle.generateBundleKey(action.getClass(), locale);
            if (actionToResourceBundle.containsKey(actionBundleKey)) {
                final ResourceBundle resourceBundle = actionToResourceBundle.get(actionBundleKey);
                LOGGER.trace("Cache hit for action '{}': '{}'", action, resourceBundle);
                return resourceBundle;
            }
            // Lookup for resource bundle in package hierarchy
            final Package actionPackage = action.getClass().getPackage();
            String currentPackageName = actionPackage.getName();

            while (currentPackageName.contains(".")) {
                try {
                    bundle = ResourceBundle.getBundle(currentPackageName + '.' + ACTIONS_MESSAGES, locale);
                    break; // Found, exit lookup
                } catch (MissingResourceException e) {
                    LOGGER.debug("No action resource bundle found for action '{}' at '{}'", action, currentPackageName, e);
                }
                currentPackageName = StringUtils.substringBeforeLast(currentPackageName, ".");
            }
        }
        if (bundle == null) {
            LOGGER.debug("Choose default action resource bundle for action '{}'", action);
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        }
        actionToResourceBundle.putIfAbsent(actionBundleKey, bundle);
        return bundle;
    }

    private void setDocumentationUrlBase(String documentationUrlBase) {
        this.documentationUrlBase = documentationUrlBase;
    }

    private String getDocumentationUrlBase() {
        return documentationUrlBase;
    }
}
