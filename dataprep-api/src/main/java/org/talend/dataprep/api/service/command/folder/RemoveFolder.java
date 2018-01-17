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

package org.talend.dataprep.api.service.command.folder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_DELETE_FOLDER;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope("request")
public class RemoveFolder extends GenericCommand<ResponseEntity<String>> {

    /**
     * Remove a folder
     *
     * @param id the folder id to remove.
     */
    public RemoveFolder(final String id) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> onExecute(id));
        onError(e -> new TDPException(UNABLE_TO_DELETE_FOLDER, e, ExceptionContext.build()));
        on(OK).then((req, resp) -> getResponseEntity(HttpStatus.OK, resp));
        on(CONFLICT).then((req, resp) -> getResponseEntity(HttpStatus.CONFLICT, resp));
    }

    private ResponseEntity<String> getResponseEntity(HttpStatus status, HttpResponse response) {

        final MultiValueMap<String, String> headers = new HttpHeaders();
        for (Header header : response.getAllHeaders()) {
            headers.put(header.getName(), Collections.singletonList(header.getValue()));
        }
        try {
            return new ResponseEntity<>(IOUtils.toString(response.getEntity().getContent(), UTF_8), headers,
                    status);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    private HttpRequestBase onExecute(final String id) {
        try {
            final URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/folders/" + id);
            return new HttpDelete(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
