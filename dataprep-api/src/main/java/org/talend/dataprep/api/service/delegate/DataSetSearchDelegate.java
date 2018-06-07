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

package org.talend.dataprep.api.service.delegate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.adapter.ApiDatasetClient;

import java.util.stream.Stream;

/**
 * A {@link SearchDelegate} implementation to search in datasets.
 */
@Component
public class DataSetSearchDelegate extends AbstractSearchDelegate<DataSetMetadata> {

    @Autowired
    private ApiDatasetClient datasetClient;

    @Override
    public String getSearchCategory() {
        return "dataset";
    }

    @Override
    public String getSearchLabel() {
        return "dataset";
    }

    @Override
    public String getInventoryType() {
        return "dataset";
    }

    @Override
    public Stream<DataSetMetadata> search(String name, boolean strict) {
        return datasetClient.searchDataset(name, strict);
    }
}
