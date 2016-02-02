//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.command.folder;

import static org.talend.dataprep.api.service.command.common.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_CREATE_FOLDER;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope("request")
public class CreateChildFolder extends GenericCommand<InputStream> {

    public CreateChildFolder(HttpClient client, String path) {
        super(APIService.DATASET_GROUP, client);
        execute(() -> onExecute(path));
        onError(e -> new TDPException(UNABLE_TO_CREATE_FOLDER, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(pipeStream());
    }

    private HttpRequestBase onExecute( String path) {
        try {

            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/folders");
            uriBuilder.addParameter("path", path);
            return new HttpPut(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
