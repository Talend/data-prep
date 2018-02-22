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

package org.talend.dataprep.async;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Class used by the AsyncAspectTest
 */
public class InvalidGroupIdGenerator implements GroupIdGenerator {

    @Override
    public String getGroupId(ProceedingJoinPoint pjp) {
        throw new IllegalArgumentException("I won't give you a group id");
    }
}
