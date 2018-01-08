// ============================================================================
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

import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;

/**
 * Check if any preparation use the dataset
 */
@Component
@Scope("request")
public class CheckDatasetUsage extends GenericCommand<Boolean> {

    /**
     * Default constructor.
     *
     * @param dataSetId The dataset id to delete.
     */
    private CheckDatasetUsage(String dataSetId) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> onExecute(dataSetId));
        on(HttpStatus.NO_CONTENT).then((httpRequestBase, httpResponse) -> true);
        on(HttpStatus.NOT_FOUND).then((httpRequestBase, httpResponse) -> false);
    }

    private HttpRequestBase onExecute(String dataSetId) {
        return new HttpHead(preparationServiceUrl + "/preparations/use/dataset/" + dataSetId);
    }

}
