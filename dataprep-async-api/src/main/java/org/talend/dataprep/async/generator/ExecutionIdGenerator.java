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

package org.talend.dataprep.async.generator;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Defines the interface to get async operation's id.
 */
public interface ExecutionIdGenerator {

    /**
     * Return the execution id from the given async operation arguments.
     *
     * @param pjp the async operation join point.
     * @return the exeuction id from the given async operation arguments.
     */
    String getExecutionId(ProceedingJoinPoint pjp);

}
