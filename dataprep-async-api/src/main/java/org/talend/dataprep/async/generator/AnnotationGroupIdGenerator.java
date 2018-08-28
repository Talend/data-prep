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
import org.aspectj.lang.reflect.MethodSignature;
import org.talend.dataprep.async.AnnotationUtils;
import org.talend.dataprep.async.AsyncGroupId;
import org.talend.dataprep.async.AsyncGroupKey;
import org.talend.dataprep.async.GroupIdGenerator;

/**
 * Implementation of the GroupIdGenerator interface that look for the @AsyncGroupId annotation in the method signature
 * to get the async operation group id.
 *
 * @see AsyncGroupId
 */
public class AnnotationGroupIdGenerator implements GroupIdGenerator {

    /**
     * @see GroupIdGenerator#getGroupId(ProceedingJoinPoint)
     */
    @Override
    public String getGroupId(ProceedingJoinPoint pjp) {

        // look for the @AsyncGroupId annotated parameter
        int idParameterIndex = AnnotationUtils.getAnnotatedParameterIndex(pjp, AsyncGroupId.class);

        // to get the @AsyncGroupId parameter value
        final String asyncGroupId;
        if (idParameterIndex >= 0) {
            MethodSignature ms = (MethodSignature) pjp.getSignature();
            final Class groupIdParameterType = ms.getParameterTypes()[idParameterIndex];
            if (AsyncGroupKey.class.isAssignableFrom(groupIdParameterType)) {
                final AsyncGroupKey paramValue = (AsyncGroupKey) pjp.getArgs()[idParameterIndex];
                asyncGroupId = paramValue.getAsyncGroupKey();
            } else {
                asyncGroupId = String.valueOf(pjp.getArgs()[idParameterIndex]);
            }
        } else {
            asyncGroupId = null;
        }
        return asyncGroupId;
    }
}
