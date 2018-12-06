// ============================================================================
//
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

package org.talend.dataprep.api.service.command.folder;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import java.net.URISyntaxException;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_DELETE_FOLDER;

@Component
@Scope(SCOPE_PROTOTYPE)
public class RemoveFolder extends GenericCommand<ResponseEntity<String>> {

    /**
     * Remove a folder
     *
     * @param folderId the folder id to remove.
     */
    public RemoveFolder(final String folderId) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> onExecute(folderId));
        onError(e -> new TDPException(UNABLE_TO_DELETE_FOLDER, e, ExceptionContext.build().put("folderId", folderId)));
        on(OK).then((req, resp) -> Defaults.getResponseEntity(NO_CONTENT, resp));
        on(NOT_FOUND).then((req, resp) -> Defaults.getResponseEntity(NOT_FOUND, resp));
        on(CONFLICT).then((req, resp) -> Defaults.getResponseEntity(CONFLICT, resp));
    }

    private HttpRequestBase onExecute(final String folderId) {
        try {
            final URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/folders/" + folderId);
            return new HttpDelete(uriBuilder.build());
        } catch (URISyntaxException e) {
            final ExceptionContext context = ExceptionContext.build().put("folderId", folderId);
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e, context);
        }
    }

}
