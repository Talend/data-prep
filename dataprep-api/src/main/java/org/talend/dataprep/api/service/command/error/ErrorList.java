//  ============================================================================
//
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

package org.talend.dataprep.api.service.command.error;

import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Command used to list the supported error codes from low level api.
 */
@Component
@Scope("prototype")
public class ErrorList extends GenericCommand<InputStream> {

    /**
     * Private constructor.
     *
     * @param type the api type.
     * @param groupKey the command group key.
     */
    private ErrorList(HystrixCommandGroupKey groupKey, ServiceType type) {
        super(groupKey);
        execute(() -> onExecute(type));
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_LIST_ERRORS, e));
        on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(emptyStream());
        on(HttpStatus.OK).then(pipeStream());
    }

    private HttpRequestBase onExecute(ServiceType type) {
        final String serviceUrl;
        switch (type) {
        case DATASET:
            serviceUrl = datasetServiceUrl + "/datasets/errors";
            break;
        case TRANSFORMATION:
            serviceUrl = transformationServiceUrl + "/transform/errors";
            break;
        case PREPARATION:
            serviceUrl = preparationServiceUrl + "/preparations/errors";
            break;
        default:
            throw new IllegalArgumentException("Type '" + type + "' is not supported.");
        }
        return new HttpGet(serviceUrl);
    }

    public enum ServiceType {
                             DATASET,
                             TRANSFORMATION,
                             PREPARATION
    }

}
