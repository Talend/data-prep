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

package org.talend.dataprep.api.service.command.dataset;

import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.Import;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;

import com.fasterxml.jackson.core.type.TypeReference;

import javax.annotation.PostConstruct;

/**
 * Command to list dataset import types.
 */
@Component
@Scope("request")
public class DataSetGetImports extends GenericCommand<List<Import>> {

    public DataSetGetImports() {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> new HttpGet(datasetServiceUrl + "/datasets/imports"));
    }

    @PostConstruct
    public void init() {
        on(HttpStatus.OK).then(Defaults.convertResponse(objectMapper, new TypeReference<List<Import>>() {
        }));
    }
}
