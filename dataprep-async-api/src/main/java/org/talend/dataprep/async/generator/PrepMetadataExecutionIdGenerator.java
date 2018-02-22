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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Validate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.talend.dataprep.async.AnnotationUtils;

public class PrepMetadataExecutionIdGenerator implements ExecutionIdGenerator {

    @Override
    public String getExecutionId(ProceedingJoinPoint pjp) {

        // look for AsyncParameter param
        Object[] args = AnnotationUtils.extractAsyncParameter(pjp);

        // check pre-condition
        Validate.notNull(args);
        Validate.isTrue(args.length == 2);
        Validate.isInstanceOf(String.class, args[0]);
        Validate.isInstanceOf(String.class, args[1]);

        return DigestUtils.sha1Hex(args[0] + "_" + args[1]);
    }
}
