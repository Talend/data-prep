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

import java.io.InputStream;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;

@Component
@Scope("request")
public class UpdateDataSet extends GenericCommand<String> {

    private UpdateDataSet(String id, InputStream dataSetContent) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> {
            final HttpPut put = new HttpPut(datasetServiceUrl + "/datasets/" + id); //$NON-NLS-1$ //$NON-NLS-2$
            put.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            put.setEntity(new InputStreamEntity(dataSetContent));
            return put;
        });
        on(HttpStatus.NO_CONTENT).then(emptyString());
        on(HttpStatus.OK).then(asString());
    }

}
