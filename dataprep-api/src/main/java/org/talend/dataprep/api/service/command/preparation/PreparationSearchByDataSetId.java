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

package org.talend.dataprep.api.service.command.preparation;

import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * Command used to retrieve the preparations based on a dataset.
 */
@Component
@Scope("prototype")
public class PreparationSearchByDataSetId extends GenericCommand<InputStream> {

    /**
     * Private constructor used to construct the generic command used to list of preparations based on a dataset id.
     *
     * @param datasetId the dataset id.
     */
    private PreparationSearchByDataSetId(String datasetId) {
        super(GenericCommand.PREPARATION_GROUP);
        execute(() -> {
            try {
                URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/preparations/search");
                uriBuilder.addParameter("dataSetId", datasetId);
                return new HttpGet(uriBuilder.build());
            } catch (URISyntaxException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        });
        on(HttpStatus.OK).then(pipeStream());
    }

}
