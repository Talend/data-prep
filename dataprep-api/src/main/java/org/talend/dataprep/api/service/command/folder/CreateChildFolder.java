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

import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope("request")
public class CreateChildFolder extends GenericCommand<InputStream> {

    /**
     * Create a folder
     *
     * @param parentId the parent folder id.
     * @param path the folder relative path to create
     */
    public CreateChildFolder(final String parentId, final String path) {
        super(GenericCommand.PREPARATION_GROUP);
        execute(() -> onExecute(parentId, path));
        on(HttpStatus.OK).then(pipeStream());
    }

    private HttpRequestBase onExecute(final String parentId, final String path) {
        try {

            URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/folders");
            if (parentId != null) {
                uriBuilder.addParameter("parentId", parentId);
            }
            uriBuilder.addParameter("path", path);
            return new HttpPut(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
