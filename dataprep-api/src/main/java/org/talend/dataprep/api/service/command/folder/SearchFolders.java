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
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

@Component
@Scope("request")
public class SearchFolders extends GenericCommand<InputStream> {

    private SearchFolders(final String name, final Boolean strict, String path) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> onExecute(name, strict, path));
        on(HttpStatus.OK).then(pipeStream());
    }

    private HttpRequestBase onExecute(final String name, final Boolean strict, String path) {
        try {

            URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/folders/search");
            if (name != null) {
                uriBuilder.addParameter("name", name);
            }
            if (strict != null) {
                uriBuilder.addParameter("strict", String.valueOf(strict));
            }
            if (path != null) {
                uriBuilder.addParameter("path", path);
            }
            return new HttpGet(uriBuilder.build());

        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
    }

}
