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

package org.talend.dataprep.api.service.command;

import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.async.AsyncExecutionMessage;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import java.io.IOException;
import java.io.InputStream;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class QueueStatusCommand extends GenericCommand<AsyncExecutionMessage> {

    private QueueStatusCommand(ServiceType service, String idQueue) {
        super(ASYNC_GROUP);
        execute(() -> {
            final String serviceUrl = getServiceUrl(service);
            return new HttpGet(serviceUrl + "/queue/" + idQueue);
        });
        on(HttpStatus.OK).then((request, response) -> {
            try (InputStream content = response.getEntity().getContent()) {
                return objectMapper.readerFor(AsyncExecutionMessage.class).readValue(content);
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            } finally {
                request.releaseConnection();
            }
        });
    }

}
