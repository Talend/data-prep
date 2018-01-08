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

package org.talend.dataprep.exception;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.util.AspectHelper;

import com.netflix.hystrix.exception.HystrixRuntimeException;

import io.swagger.annotations.ApiOperation;

@Configuration
@Aspect
@SuppressWarnings("InsufficientBranchCoverage")
class Aspects {

    private static final Logger LOG = LoggerFactory.getLogger(Aspects.class);

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) && @annotation(io.swagger.annotations.ApiOperation)")
    public Object exception(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (TalendRuntimeException e) {
            throw e; // Let TDPException pass through (to be processed in correct HTTP code by controller advice).
        } catch (HystrixRuntimeException hre) {
            throw TDPExceptionUtils.processHystrixException(hre);
        } catch (Exception e) {
            LOG.error("Unexpected exception occurred in '" + pjp.getSignature().toShortString() + "'", e);
            final ExceptionContext context = ExceptionContext.build();
            final ApiOperation apiOperation = AspectHelper.getAnnotation(pjp, ApiOperation.class);
            if (apiOperation != null && !StringUtils.isEmpty(apiOperation.value())) {
                // Build message as a "Unable to " + <api operation description> (with first character as lower case).
                String messageSuffix = apiOperation.value();
                if (Character.isUpperCase(messageSuffix.charAt(0))) {
                    messageSuffix = Character.toLowerCase(messageSuffix.charAt(0)) + messageSuffix.substring(1);
                }
                context.put("message", "Unable to " + messageSuffix);
            }
            throw new TDPException(CommonErrorCodes.UNEXPECTED_SERVICE_EXCEPTION, e, context);
        }
    }

}
