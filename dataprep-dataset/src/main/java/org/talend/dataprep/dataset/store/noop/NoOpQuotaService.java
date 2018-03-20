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

package org.talend.dataprep.dataset.store.noop;

import static org.slf4j.LoggerFactory.getLogger;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;
import org.talend.dataprep.dataset.store.QuotaService;

/**
 * [Personal & Enterprise Edition] Does nothing.
 */
@Component
@Conditional(NoOpQuotaService.class)
public class NoOpQuotaService implements QuotaService, Condition {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(NoOpQuotaService.class);

    @PostConstruct
    public void init() {
        LOGGER.info("No quota applied");
    }

    @Override
    public void checkIfAddingSizeExceedsAvailableStorage(long size) {
        // Do nothing
    }


    /**
     * @return true if 'dataset.quota.check.enabled' is not set to 'true'
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final String property = context.getEnvironment().getProperty("dataset.quota.check.enabled");
        return !StringUtils.equals("true", property);
    }

    /**
     * @return Long.MAX_VALUE
     */
    @Override
    public long getAvailableSpace() {
        return Long.MAX_VALUE;
    }
}
