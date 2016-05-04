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

import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * List sub folders for the given path.
 */
@Component
@Scope("prototype")
public class FoldersList extends GenericCommand<InputStream> {

    /**
     * List child folders for the given path.
     *
     * @param path the path to list the folder from.
     * @param sort the sort key.
     * @param order how to use the sort key.
     */
    // private constructor to ensure IoC
    private FoldersList(String path, String sort, String order) {
        super(GenericCommand.PREPARATION_GROUP);
        execute(() -> onExecute(path, sort, order));
        on(HttpStatus.OK).then(pipeStream());
    }

    /**
     * List child folders for the given path.
     *
     * @param path the path to list the folder from.
     */
    // private constructor to ensure IoC
    private FoldersList(String path) {
        this(path, null, null);
    }

    private HttpRequestBase onExecute(String path, String sort, String order) {
        try {

            URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/folders" );

            if (StringUtils.isNotBlank(path)) {
               uriBuilder.addParameter("path", path);
            }
            if (StringUtils.isNotBlank(sort)) {
                uriBuilder.addParameter("sort", sort);
            }
            if (StringUtils.isNotBlank(order)) {
                uriBuilder.addParameter("order", order);
            }

            return new HttpGet(uriBuilder.build());

        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
