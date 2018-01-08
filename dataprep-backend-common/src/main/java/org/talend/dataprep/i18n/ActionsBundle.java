// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Non-spring accessor to actions resources bundle. This implementation use a simple caching mechanism based on action, locale and key searched.
 * Each key is searched using a fallback mechanism that search in this order:
 * <ol>
 *     <li>in the current supplied action package</li>
 *     <li>in the current supplied action package hierarchy (org.talend.dataprep.transformation.actions.MyAction, org.talend.dataprep.transformation.actions)</li>
 *     <li>in this bundle package</li>
 *     <li>in this bundle hierarchy</li>
 *     <li>in the default {@code actions_messages} bundle</li>
 * </ol>
 * For instance, {@code org.talend.dataprep.transformation.actions.MyAction} will be searched in:
 * <ol>
 *     <li>org.talend.dataprep.transformation.actions.MyAction</li>
 *     <li>org.talend.dataprep.transformation.actions</li>
 *     <li>org.talend.dataprep.transformation</li>
 *     <li>org.talend.dataprep</li>
 *     <li>org.talend</li>
 *     <li>org</li>
 *     <li>org.talend.dataprep.i18n.ActionsBundle</li>
 *     <li>org.talend.dataprep.i18n</li>
 *     <li>org.talend.dataprep.i18n.actions_messages</li>
 * </ol>
 *
 *
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

    /** Base URL of the documentation portal. */
    // Documentation URL is not thread safe. It is acceptable while it is only changed at the application startup.
    // If more changes is to happen, there should be a thread safety mechanism
    private String documentationUrlBase;

    /**
     * Simple cache.
     */
    private Cache<CacheKey, String> cache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

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
     * exist, code will lookup in fallback resource bundle (i.e. Data Prep one) for message.
     */
    public static String actionLabel(Object action, Locale locale, String actionName, Object... values) {
        final String actionLabelKey = ACTION_PREFIX + actionName + LABEL_SUFFIX;
        return INSTANCE.getMandatoryMessage(action, locale, actionLabelKey, values);
    }

    /**
     * Fetches action description at {@code action.<action_name>.desc} in the dataprep actions resource bundle. If message does
     * not exist, code will lookup in fallback resource bundle (i.e. Data Prep one) for message.
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
     * exist, code will lookup in fallback resource bundle (i.e. Data Prep one) for message.
     */
    public static String categoryName(Object action, Locale locale, String categoryName, Object... values) {
        final String categoryLabelKey = CATEGORY_PREFIX + categoryName + LABEL_SUFFIX;
        return INSTANCE.getMandatoryMessage(action, locale, categoryLabelKey, values);
    }

    /**
     * Fetches parameter label at {@code parameter.<parameter_name>.label} in the dataprep actions resource bundle. If message
     * does not exist, code will lookup in fallback resource bundle (i.e. Data Prep one) for message.
     */
    public static String parameterLabel(Object action, Locale locale, String parameterName, Object... values) {
        final String parameterLabelKey = PARAMETER_PREFIX + parameterName + LABEL_SUFFIX;
        return INSTANCE.getMandatoryMessage(action, locale, parameterLabelKey, values);
    }

    /**
     * Fetches parameter description at {@code parameter.<parameter_name>.desc} in the dataprep actions resource bundle. If
     * message does not exist, code will lookup in fallback resource bundle (i.e. Data Prep one) for message.
     */
    public static String parameterDescription(Object action, Locale locale, String parameterName, Object... values) {
        final String parameterDescriptionKey = PARAMETER_PREFIX + parameterName + DESCRIPTION_SUFFIX;
        return INSTANCE.getMandatoryMessage(action, locale, parameterDescriptionKey, values);
    }

    /**
     * Fetches choice at {@code choice.<choice_name>} in the dataprep actions resource bundle. If message does not exist, code
     * will lookup in fallback resource bundle (i.e. Data Prep one) for message.
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
        return getMandatoryMessage(this, locale, code, args);
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
        String messageFormat = getBundleValue(action, locale, code);
        return  messageFormat == null ? null : formatMessage(messageFormat, args);
    }

    private String getBundleValue(Object action, Locale locale, String code) {
        CacheKey cacheKey = new CacheKey(action, locale, code);
        String bundleMessage = cache.getIfPresent(cacheKey);
        if (bundleMessage == null) {
            final ResourceBundle bundle = findBundleContainingKey(action, locale, code, getBundlesFallbackList(action));
            if (Objects.nonNull(bundle)) {
                bundleMessage = bundle.getString(code);
                cache.put(cacheKey, bundleMessage);
            } else {
                bundleMessage = null;
            }
        }
        return bundleMessage;
    }

    /**
     * Get the message from bundle or fallback bundle.
     * Ig message is not present, a TalendRuntimeException is thrown
     */
    private String getMandatoryMessage(Object action, Locale locale, String code, Object... args) {
        final String message = getOptionalMessage(action, locale, code, args);
        if (message == null) {
            throw new TalendRuntimeException(BaseErrorCodes.MISSING_I18N, ExceptionContext.withBuilder() .put("code", code).put("action", action).build());
        }
        return message;
    }

    /**
     * Searches the list of bundles and returns the first that contains the key. <bold>warning:</bold> if the key is found
     * in a bundle in a fallback language before being present in another bundle in the correct language the first will be
     * returned.
     */
    private ResourceBundle findBundleContainingKey(Object action, Locale locale, String key, Collection<String> bundleFallbacks) {
        ResourceBundle bundle = null;
        Iterator<String> iterator = bundleFallbacks.iterator();
        while (iterator.hasNext() && bundle == null) {
            String packageName = iterator.next();
            try {
                ResourceBundle searchedBundle = ResourceBundle.getBundle(packageName + '.' + ACTIONS_MESSAGES, //
                        locale, //
                        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
                );
                if (searchedBundle.containsKey(key)) {
                    bundle = searchedBundle;
                }
            } catch (MissingResourceException e) {
                LOGGER.debug("No action resource bundle found for action '{}' at '{}'", action, packageName, e);
            }
        }
        return bundle;
    }

    /**
     * Create the list of bundle names fallback to search for i18n keys:
     * <ul>
     * <li> first add the full package hierarchy of the action class</li>
     * <li> then add the full package hierarchy of this bundle class</li>
     * <li> and last but not least, add the default {@link #BUNDLE_NAME}</li>
     * </ul>
     */
    private Collection<String> getBundlesFallbackList(Object action) {
        LinkedHashSet<String> packageHierarchy = new LinkedHashSet<>();;
        if (Objects.nonNull(action)) {
            packageHierarchy.addAll(Arrays.asList(getPackageHierarchy(action.getClass())));
        }

        packageHierarchy.addAll(Arrays.asList(getPackageHierarchy(this.getClass())));
        packageHierarchy.add(BUNDLE_NAME);
        return packageHierarchy;
    }

    /**
     * Extract the full package hierarchy of a class.
     */
    private String[] getPackageHierarchy(Class<?> clazz) {
        String packageName = clazz.getPackage().getName();
        String[] hierarchy = new String[StringUtils.countMatches(packageName, ".") + 1];
        for (int i = 0; i < hierarchy.length; i++) {
            hierarchy[i] = packageName;
            packageName = StringUtils.substringBeforeLast(packageName, ".");
        }
        return hierarchy;
    }

    private void setDocumentationUrlBase(String documentationUrlBase) {
        this.documentationUrlBase = documentationUrlBase;
    }

    private String getDocumentationUrlBase() {
        return documentationUrlBase;
    }

    private static class CacheKey {

        private final Object action;

        private final Locale locale;

        private final String code;

        private CacheKey(Object action, Locale locale, String code) {
            this.action = action;
            this.locale = locale;
            this.code = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(action, cacheKey.action) && Objects.equals(locale, cacheKey.locale) && Objects.equals(code,
                    cacheKey.code);
        }

        @Override
        public int hashCode() {
            return Objects.hash(action, locale, code);
        }
    }
}
