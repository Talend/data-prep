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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.api.service.command.common.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_CREATE_FOLDER_ENTRY;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.core.JsonProcessingException;

@Component
@Scope("request")
public class CreateFolderEntry extends GenericCommand<InputStream> {

    public CreateFolderEntry(HttpClient client, FolderEntry folderEntry) {
        super(APIService.DATASET_GROUP, client);
        execute(() -> onExecute(folderEntry));
        onError(e -> new TDPException(UNABLE_TO_CREATE_FOLDER_ENTRY, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(pipeStream());
    }

    private HttpRequestBase onExecute(FolderEntry folderEntry) {
        try {

            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/folders/entries");

            HttpPut create = new HttpPut(uriBuilder.build());
            create.addHeader("Content-Type", APPLICATION_JSON_VALUE);
            byte[] theBytes = builder.build().writeValueAsBytes(folderEntry);
            create.setEntity(new ByteArrayEntity(theBytes));
            return create;
        } catch (URISyntaxException | JsonProcessingException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
