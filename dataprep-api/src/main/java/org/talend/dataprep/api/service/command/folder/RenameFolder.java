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

package org.talend.dataprep.api.service.command.folder;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_RENAME_FOLDER;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope(SCOPE_PROTOTYPE)
public class RenameFolder extends GenericCommand<Void> {

    public RenameFolder(final String folderId, final String newName) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> onExecute(folderId, newName));
        onError(e -> new TDPException(UNABLE_TO_RENAME_FOLDER, e, ExceptionContext.build().put("folderId", folderId)));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute(final String folderId, final String newName) {
        try {
            final URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/folders/" + folderId + "/name");
            uriBuilder.addParameter("newName", newName);
            final HttpPut put = new HttpPut(uriBuilder.build());
            put.setEntity(new StringEntity(newName));
            return put;
        } catch (UnsupportedEncodingException | URISyntaxException e) {
            final ExceptionContext context = ExceptionContext.build().put("folderId", folderId);
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e, context);
        }
    }

}
