// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import org.apache.commons.lang.StringUtils;
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
