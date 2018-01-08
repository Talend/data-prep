// ============================================================================
//
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

package org.talend.dataprep.api.service.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.security.Security;

import com.netflix.hystrix.HystrixCommand;

/**
 * Configurer that modify a Setting
 *
 * @param <T> (ViewSettings | ActionSettings)
 */
public abstract class AppSettingsConfigurer<T> {

    /** The spring application context. */
    @Autowired
    private ApplicationContext context;

    /** To get access to the current connected user. */
    @Autowired
    private Security security;

    protected static final Logger LOGGER = LoggerFactory.getLogger(AppSettingsConfigurer.class);

    /**
     * Check if this AppSettingsConfigurer is applicable to the T setting.
     *
     * @param setting The setting to test.
     * @return true if it is applicable, false otherwise.
     */
    public abstract boolean isApplicable(final T setting);

    /**
     * Apply custom configuration to the ActionSettings
     *
     * @param setting The settings to modify
     * @return The resulting T setting
     */
    public abstract T configure(final T setting);

    /**
     * @return true if the current user is allowed for this setting configuration
     */
    public boolean isUserAuthorized() {
        // default is TDP users
        return security.isTDPUser();
    }

    /**
     * Get hystrix command
     */
    protected <S extends HystrixCommand> S getCommand(Class<S> clazz, Object... args) {
        try {
            return context.getBean(clazz, args);
        } catch (BeansException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_FIND_COMMAND, e,
                    ExceptionContext.build().put("class", clazz).put("args", args));
        }
    }

}
