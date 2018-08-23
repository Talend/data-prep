package org.talend.dataprep.api.service.command.common;

import com.netflix.hystrix.HystrixCommandGroupKey;
import org.apache.http.client.methods.HttpGet;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
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
        // TODO : service URL must not be here
        execute(() -> new HttpGet(transformationServiceUrl + asyncMethodStatusUrl));
        on(HttpStatus.OK).then(Defaults.convertResponse(objectMapper, AsyncExecutionMessage.class));
        onError(e -> new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e));
    }
}
