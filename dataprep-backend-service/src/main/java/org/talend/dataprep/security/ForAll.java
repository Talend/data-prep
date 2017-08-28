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

/**
 * An interface to abstract repetitive actions (to be performed on all tenants for example).
 */
public interface ForAll {

    /**
     * Execute the provided <code>runnable</code> for all tenants.
     *
     * @param runnable The {@link Runnable} to execute.
     */
    void execute(final Supplier<Boolean> condition, Runnable runnable);

    /**
     * @return A {@link ForAllConditionBuilder builder} for building conditions to {@link #execute(Supplier, Runnable)}.
     */
    ForAllConditionBuilder condition();

    /**
     * Execute the provided <code>runnable</code> for all tenants.
     *
     * @param runnable The {@link Runnable} to execute.
     */
    default void execute(Runnable runnable) {
        execute(() -> true, runnable);
    }

    /**
     * A builder for {@link #execute(Supplier, Runnable)}.
     */
    interface ForAllConditionBuilder {

        /**
         * <p>
         * Checks if a bean can operate in current context. For example, implementation may check tenancy information
         * has all required configuration.
         * </p>
         *
         * @param bean The Spring bean to be check.
         * @return A check if bean can operate in current context.
         */
        Supplier<Boolean> operational(Object bean);

    }

}
