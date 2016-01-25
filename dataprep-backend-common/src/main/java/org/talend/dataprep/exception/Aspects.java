package org.talend.dataprep.exception;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.elasticsearch.common.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import io.swagger.annotations.ApiOperation;

@Configuration
@Aspect
@SuppressWarnings("InsufficientBranchCoverage")
class Aspects {

    private static final Logger LOG = LoggerFactory.getLogger(Aspects.class);

    @Around("execution(* *(..)) && @annotation(requestMapping) && @annotation(apiOperation)")
    public Object exception(ProceedingJoinPoint pjp, RequestMapping requestMapping, ApiOperation apiOperation) throws Throwable {
        try {
            return pjp.proceed(pjp.getArgs());
        } catch (TDPException e) {
            throw e; // Let TDPException pass through (to be processed in correct HTTP code by controller advice).
        } catch (HystrixRuntimeException hre) {
            // filter out hystrix exception level if possible
            Throwable e = hre;
            while (e.getCause() != null) {
                e = e.getCause();
                if (e instanceof TDPException) {
                    throw e;
                }
            }
            throw hre;
        } catch (Exception e) {
            LOG.error("Unexpected exception occurred in '" + pjp.getSignature().toShortString() + "'", e);
            final ExceptionContext context = ExceptionContext.build();
            if(apiOperation != null && !StringUtils.isEmpty(apiOperation.value())) {
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
