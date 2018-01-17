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

import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;

/**
 * Command to list dataset import types.
 */
@Component
@Scope("request")
public class DataSetGetImportParameters extends GenericCommand<InputStream> {

    /**
     * Constructor.
     */
    public DataSetGetImportParameters(String importType) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> new HttpGet(datasetServiceUrl + "/datasets/imports/" + importType + "/parameters"));
        on(HttpStatus.NO_CONTENT).then(emptyStream());
        on(HttpStatus.OK).then(pipeStream());
    }

}
