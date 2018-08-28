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

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.talend.dataprep.async.AnnotationUtils;
import org.talend.dataprep.async.AsyncExecutionId;
import org.talend.dataprep.async.generator.ExecutionIdGenerator;

/**
 * An annotation-based implementation of {@link ExecutionIdGenerator}. Returns the value as String of parameter
 * annotated with {@link AsyncExecutionId} or returns a new UUID each time {@link #getExecutionId(ProceedingJoinPoint)}
 * if no {@link AsyncExecutionId} is present in wrapped method parameters.
 */
public class AnnotationExecutionIdGenerator implements ExecutionIdGenerator {

    @Override
    public String getExecutionId(ProceedingJoinPoint pjp) {
        // look for the @AsyncExecutionId annotated parameter
        int idParameterIndex = AnnotationUtils.getAnnotatedParameterIndex(pjp, AsyncExecutionId.class);

        // to get the @AsyncGroupId parameter value
        if (idParameterIndex >= 0) {
            return String.valueOf(pjp.getArgs()[idParameterIndex]);
        }
        return UUID.randomUUID().toString();
    }

}
