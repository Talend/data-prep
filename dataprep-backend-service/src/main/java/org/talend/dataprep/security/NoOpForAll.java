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

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A fall back implementation of {@link ForAll} in case code is running with no tenancy enabled.
 */
@Component
public class NoOpForAll implements ForAll {

    @Override
    public void execute(Supplier<Boolean> condition, Runnable runnable) {
        if (condition.get()) {
            runnable.run();
        } else {
            LoggerFactory.getLogger(ForAll.class).debug("Unable to run '{}' (condition disallowed run of it).", runnable);
        }
    }

    @Override
    public ForAllConditionBuilder condition() {
        // This ForAllConditionBuilder implementation always returns a Supplier that returns true
        return bean -> () -> true;
    }
}
