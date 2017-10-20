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

package org.talend.dataprep.security;

import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * A fall back implementation of {@link ForAll} in case code is running with no tenancy enabled.
 */
@Component
@ConditionalOnProperty(name = "task.scheduled.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpForAll implements ForAll {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpForAll.class);

    @PostConstruct
    public void init() {
        LOGGER.info("ForAll: multi tenancy disabled.");
    }

    @Override
    public void execute(Supplier<Boolean> condition, Runnable runnable) {
        try {
            if (condition.get()) {
                runnable.run();
            } else {
                LOGGER.debug("Unable to run '{}' (condition disallowed run of it).", runnable);
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to execute run '{}'. Skip execution.", runnable);
            LOGGER.debug("Unable to execute run '{}'. Skip execution error.", e);
        }
    }

    @Override
    public ForAllConditionBuilder condition() {
        // This ForAllConditionBuilder implementation always returns a Supplier that returns true
        return bean -> () -> true;
    }
}
