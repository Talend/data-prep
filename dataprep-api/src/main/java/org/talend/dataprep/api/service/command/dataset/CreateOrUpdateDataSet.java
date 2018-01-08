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

package org.talend.dataprep.api.service.command.dataset;

import static org.talend.dataprep.command.Defaults.asString;
import static org.talend.dataprep.command.Defaults.emptyString;
import static org.talend.dataprep.command.Defaults.passthrough;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_CREATE_OR_UPDATE_DATASET;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

/**
 * Command in charge of mostly updating a dataset content.
 */
@Component
@Scope("request")
public class CreateOrUpdateDataSet extends GenericCommand<String> {

    /**
     * Private constructor.
     *
     * @param id the dataset id.
     * @param name the dataset name.
     * @param dataSetContent the new dataset content.
     */
    private CreateOrUpdateDataSet(String id, String name, long size, InputStream dataSetContent) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> {
            try {
                URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/datasets/" + id + "/raw/");
                if (!StringUtils.isEmpty(name)) {
                    uriBuilder.addParameter("name", name);
                }
                uriBuilder.addParameter("size", String.valueOf(size));
                final HttpPut put = new HttpPut(uriBuilder.build()); // $NON-NLS-1$ //$NON-NLS-2$
                put.setEntity(new InputStreamEntity(dataSetContent));
                return put;
            } catch (URISyntaxException e) {
                throw new TDPException(UNEXPECTED_EXCEPTION, e);
            }
        });
        onError(e -> {
            if (e instanceof TDPException) {
                return passthrough().apply(e);
            }
            return new TDPException(UNABLE_TO_CREATE_OR_UPDATE_DATASET, e);
        });
        on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(emptyString());
        on(HttpStatus.OK).then(asString());
    }
}
