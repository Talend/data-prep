// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_DELETE_FOLDER_ENTRY;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * TODO This is not used : should remove ?
 */
@Component
@Scope("request")
@Deprecated
public class RemoveFolderEntry extends GenericCommand<Void> {

    public RemoveFolderEntry(final String folderId, final String contentType, final String contentId) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> onExecute(folderId, contentType, contentId));
        onError(e -> new TDPException(UNABLE_TO_DELETE_FOLDER_ENTRY, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute(final String folderId, final String contentType, final String contentId) {
        try {
            final URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/folders/entries/" //
                    + contentType //
                    + '/' + contentId);
            uriBuilder.addParameter("folderId", folderId);
            return new HttpDelete(uriBuilder.build());

        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}