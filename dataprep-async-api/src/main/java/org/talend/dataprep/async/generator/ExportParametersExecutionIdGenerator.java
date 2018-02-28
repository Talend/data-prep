//  ============================================================================
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.async.generator;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.async.AnnotationUtils;


public class ExportParametersExecutionIdGenerator implements ExecutionIdGenerator {

    @Override
    public String getExecutionId(ProceedingJoinPoint pjp) {

        //look for ExportParameters param
        Object[] args = AnnotationUtils.extractAsyncParameter(pjp);

        for (Object arg : args) {
            if(arg instanceof ExportParameters){
                return ((ExportParameters) arg).generateUniqueId();
            }
        }

        return UUID.randomUUID().toString();
    }

}
