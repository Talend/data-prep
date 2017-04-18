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

/**
 * An interface to abstract repetitive actions (to be performed on all tenants for exemple).
 */
public interface ForAll {

    /**
     * Execute the provided <code>runnable</code> for all tenants.
     * @param runnable The {@link Runnable} to execute.
     */
    void execute(Runnable runnable);
}
