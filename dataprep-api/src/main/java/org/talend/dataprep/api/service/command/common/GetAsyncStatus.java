package org.talend.dataprep.api.service.command.common;

import com.netflix.hystrix.HystrixCommandGroupKey;
import org.apache.http.client.methods.HttpGet;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.async.AsyncExecutionMessage;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import javax.annotation.PostConstruct;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class GetAsyncStatus extends GenericCommand<AsyncExecutionMessage> {

    private final String asyncMethodStatusUrl;

    public static GetAsyncStatus create(ApplicationContext context, HystrixCommandGroupKey group, String asyncMethodStatusUrl) {
        return context.getBean(GetAsyncStatus.class, group, asyncMethodStatusUrl);
    }

    protected GetAsyncStatus(HystrixCommandGroupKey group, String asyncMethodStatusUrl) {
        super(group);
        this.asyncMethodStatusUrl = asyncMethodStatusUrl;
    }

    @PostConstruct
    public void init() {
        final String serviceUrl;
        HystrixCommandGroupKey commandGroup = getCommandGroup();
        if (commandGroup == TRANSFORM_GROUP) {
            serviceUrl = transformationServiceUrl;
        } else if (commandGroup == DATASET_GROUP) {
            serviceUrl = datasetServiceUrl;
        } else if (commandGroup == PREPARATION_GROUP) {
            serviceUrl = preparationServiceUrl;
        } else if (commandGroup == FULLRUN_GROUP) {
            serviceUrl = fullRunServiceUrl;
        } else {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION,
                    ExceptionContext.withBuilder().put("message", "unknown service" + commandGroup).build());
        }

        execute(() -> new HttpGet(serviceUrl + asyncMethodStatusUrl));
        on(HttpStatus.OK).then(Defaults.convertResponse(objectMapper, AsyncExecutionMessage.class));
        onError(e -> new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e));
    }
}
