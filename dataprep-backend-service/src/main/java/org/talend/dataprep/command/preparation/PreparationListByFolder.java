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

package org.talend.dataprep.command.preparation;

import static org.springframework.http.HttpStatus.*;
import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_RETRIEVE_PREPARATION_LIST;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;

/**
 * Command used to retrieve the preparations from a folder.
 */
@Component
@Scope("prototype")
public class PreparationListByFolder extends GenericCommand<InputStream> {

    /**
     * Private constructor used to construct the generic command used to list of preparations matching name.
     *
     * @param folderId the folder id where to look for preparations.
     * @param sort how to sort the preparations.
     * @param order the order to apply to the sort.
     */
    private PreparationListByFolder(final String folderId, final Sort sort, final Order order) {
        super(GenericCommand.PREPARATION_GROUP);
        execute(() -> {
            try {
                final URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/preparations/search");
                uriBuilder.addParameter("folderId", folderId);
                uriBuilder.addParameter("sort", sort.camelName());
                uriBuilder.addParameter("order", order.camelName());
                return new HttpGet(uriBuilder.build());
            } catch (URISyntaxException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        });
        onError(e -> new TDPException(UNABLE_TO_RETRIEVE_PREPARATION_LIST, e));
        on(NO_CONTENT, ACCEPTED).then(emptyStream());
        on(OK).then(pipeStream());
    }

}
