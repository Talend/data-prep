//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================
package org.talend.dataprep.i18n;

import java.util.Objects;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

/**
 * This class provides i18n support and provides a simple way to access {@link ResourceBundleMessageSource} in current
 * Spring context.
 *
 * @see org.talend.dataprep.util.MessagesBundleContext To get the current message bundle when serving a request.
 * @deprecated this class is for compatibility, please use {@link DataprepBundle} instead.
 */
@Component
@Deprecated
public class MessagesBundle {

    public MessagesBundle() {}

    /**
     * Redirect to {@link DataprepBundle#message(String, Object...)}.
     */
    public String getString(String code) {
        return DataprepBundle.message(code);
    }

    /**
     * Redirect to {@link DataprepBundle#message(String, Object...)} but returns the default if no value message is returned.
     * Using a default in i18n is bad practice as there should always be at least one default message and if not, it is a bug.
     */
    public String getString(String code, String defaultMessage) {
        String message = DataprepBundle.message(code);
        if (Objects.equals(message, code)) {
            message = defaultMessage;
        }
        return message;
    }

    /**
     * Redirect to {@link DataprepBundle#message(String, Object...)}.
     */
    public String getString(String code, Object... args) {
        return DataprepBundle.message(code, args);
    }

}
