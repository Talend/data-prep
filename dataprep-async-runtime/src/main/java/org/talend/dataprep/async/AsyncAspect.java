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

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.async.conditional.ConditionalTest;
import org.talend.dataprep.async.generator.ExecutionIdGenerator;
import org.talend.dataprep.async.repository.ManagedTaskRepository;
import org.talend.dataprep.async.result.ResultUrlGenerator;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.util.AspectHelper;

/**
 * Aspect used to wrap the execution RequestMapping method into an asynchronous call.
 *
 * This aspect schedule the execution in a @{@link ManagedTaskExecutor} and returns a 202 http status at once.
 */
@Component
@Aspect
public class AsyncAspect {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncAspect.class);

    /** The task executor. */
    @Autowired
    private ManagedTaskExecutor executor;

    /** Where the tasks are stored. */
    @Autowired
    private ManagedTaskRepository repository;

    /** The spring application context. */
    @Autowired
    private ApplicationContext applicationContext;

    @Value("${async-runtime.contextPath:}")
    private String contextPath;

    /**
     * Intercept all the calls to a @RequestMapping method annotated with @AsyncOperation.
     *
     * @param pjp the proceeding join point.
     */
    @Around(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping) && @annotation(org.talend.dataprep.async.AsyncOperation)")
    public Object runAsynchronously(final ProceedingJoinPoint pjp) {

        String executionId = getExecutionId(pjp);
        AsyncExecution asyncExecution = repository.get(executionId);

        if(asyncExecution == null || asyncExecution.getStatus() != AsyncExecution.Status.RUNNING) {
            // the method is not running actually
            if(executeAsynchronously(pjp) || (asyncExecution != null && asyncExecution.getStatus() == AsyncExecution.Status.NEW)){
                // we need to launch it asynchronously or asyncMethod is on NEW status (we can  resume it)
                AsyncExecution future = scheduleAsynchroneTask(pjp, asyncExecution != null && asyncExecution.getStatus() == AsyncExecution.Status.NEW);
                LOGGER.debug("Scheduling done, Redirecting to execution queue...");

                // return at once with an HTTP 202 + location to get the progress
                set202HeaderInformation(future);
                LOGGER.debug("Redirection done.");
            } else {
                try {
                    return pjp.proceed();
                } catch (Throwable throwable) {
                    throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, throwable);
                }
            }
        } else {
            LOGGER.debug("Async Method with id {} is already running", asyncExecution.getId());
            set202HeaderInformation(asyncExecution);
        }

        return null;
    }

    private void set202HeaderInformation(AsyncExecution future) {
        HttpResponseContext.status(HttpStatus.ACCEPTED);
        String statusCheckURL = generateLocationUrl(future);
        HttpResponseContext.header(HttpHeaders.LOCATION, statusCheckURL);
        // https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.37
        // Duration in seconds before a retry. Value is 1s because it is the current front-side wait time
        HttpResponseContext.header(HttpHeaders.RETRY_AFTER, "1");
    }

    private AsyncExecution scheduleAsynchroneTask(ProceedingJoinPoint pjp, boolean resumeExistingAsyncExecution) {
        if (LOGGER.isDebugEnabled()) {
            final RequestMapping requestMapping = AspectHelper.getAnnotation(pjp, RequestMapping.class);
            LOGGER.debug("Scheduling for execution of {} ({})", pjp.getSignature().toLongString(),
                    Arrays.toString(requestMapping.path()));
        }

        // schedule the execution in the managed task executor
        @SuppressWarnings("unchecked")
        final AsyncExecution future;
        if (resumeExistingAsyncExecution) {
            future = executor.resume(toCallable(pjp), getExecutionId(pjp), getResultUrl(pjp));
        } else {
            future = executor.queue(toCallable(pjp), getExecutionId(pjp), getGroupId(pjp), getResultUrl(pjp));
        }
        return future;
    }

    private String generateLocationUrl(AsyncExecution future) {

        final String statusCheckURL;
        if (StringUtils.isEmpty(contextPath)) {
            statusCheckURL = "/" + AsyncController.QUEUE_PATH + "/" + future.getId();
        } else {
            String subContextPath = contextPath.startsWith("/") ? contextPath.substring(1) : contextPath;
            statusCheckURL = "/" + subContextPath + "/" + AsyncController.QUEUE_PATH + "/" + future.getId();
        }
        return statusCheckURL;
    }


    /**
     * Wrap the pjp result into a callable to be able to store the latter in a task executor.
     *
     * @param pjp the proceeding join point.
     * @return the callable from the pjp.
     */
    private ManagedTaskExecutor.ManagedTaskCallable toCallable(ProceedingJoinPoint pjp) {
        return new ManagedTaskExecutor.ManagedTaskCallable() {

            @Override
            public Method getMethod() {
                return ((MethodSignature) pjp.getSignature()).getMethod();
            }

            @Override
            public Object[] getArguments() {
                return pjp.getArgs();
            }

            @Override
            public Object call() throws Exception {
                try {
                    return pjp.proceed();
                } catch (TalendRuntimeException e) {
                    throw e;
                } catch (Throwable throwable) {
                    throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, throwable);
                }
            }
        };
    }

    private String getExecutionId(ProceedingJoinPoint pjp) {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method m = ms.getMethod();
        final AsyncOperation asyncOperationAnnotation = m.getAnnotation(AsyncOperation.class);

        final Class<? extends ExecutionIdGenerator> executionIdGeneratorClass = asyncOperationAnnotation
                .executionIdGeneratorClass();
        try {
            return executionIdGeneratorClass.newInstance().getExecutionId(pjp);
        } catch (Exception e) {
            LOGGER.warn("could not get the async id from {} because {}, fall back to null", pjp.toLongString(), e);
            return null;
        }
    }

    /**
     * Return the async execution group id from the given proceeding join point.
     *
     * @param pjp the proceeding join point.
     * @return the async execution group id from the proceeding join point.
     */
    private String getGroupId(ProceedingJoinPoint pjp) {

        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method m = ms.getMethod();
        final AsyncOperation asyncOperationAnnotation = m.getAnnotation(AsyncOperation.class);

        // try with the groupIdGeneratorBean first
        Class<? extends GroupIdGenerator> generatorClass = asyncOperationAnnotation.groupIdGeneratorBean();
        if (generatorClass != AsyncOperation.DEFAULT.class) {
            try {
                final GroupIdGenerator generatorBean = applicationContext.getBean(generatorClass);
                return generatorBean.getGroupId(pjp);
            } catch (Exception e) {
                LOGGER.warn("could not get the async group id form {} because {}, let's try with the group id generator class",
                        pjp.toLongString(), e);
            }
        }

        // as a fallback, let's try with the generator class
        generatorClass = asyncOperationAnnotation.groupIdGeneratorClass();
        try {
            final GroupIdGenerator idGenerator = generatorClass.newInstance();
            return idGenerator.getGroupId(pjp);
        } catch (Exception e) {
            LOGGER.warn("could not get the async group id form {} because {}, let's try with the group id generator class",
                    pjp.toLongString(), e);
        }

        return null;
    }


    /**
     * Return if we need to execute the method asynchronously by calling the conditionalClass definined on the annotation.
     * By Default call the AlwaysTrueCondtion
     * @param pjp pjp the proceeding join point.
     * @return true if we need to execute the method asynchronously. False otherwise
     */
    private Boolean executeAsynchronously(ProceedingJoinPoint pjp) {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method m = ms.getMethod();
        final AsyncOperation asyncOperationAnnotation = m.getAnnotation(AsyncOperation.class);

        Class<? extends ConditionalTest> conditionalTestGenerator = asyncOperationAnnotation.conditionalClass();

        final ConditionalTest conditionalTest = applicationContext.getBean(conditionalTestGenerator);
        Object[] args = AnnotationUtils.extractAsyncParameter(pjp);
        return conditionalTest.apply(args);
    }

    /**
     * Return the URL used to get the result of the asynchronous method
     * @param pjp pjp the proceeding join point.
     * @return the URL used to get the result of the asynchronous method
     */
    private AsyncExecutionResult getResultUrl(ProceedingJoinPoint pjp) {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method m = ms.getMethod();
        final AsyncOperation asyncOperationAnnotation = m.getAnnotation(AsyncOperation.class);

        Class<? extends ResultUrlGenerator> resultUrlClass = asyncOperationAnnotation.resultUrlGenerator();

        final ResultUrlGenerator resultUrlGenerator = applicationContext.getBean(resultUrlClass);
        Object[] args = AnnotationUtils.extractAsyncParameter(pjp);
        return resultUrlGenerator.generateResultUrl(args);
    }


}
