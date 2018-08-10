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

package org.talend.dataprep.api.service.version;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.talend.dataprep.info.Version;

import static java.util.Collections.singletonList;

@Component
@Order(value = 1)
public class DatasetVersionSupplier extends AbstractVersionSupplier {

    @Value("${dataset.service.url}")
    protected String datasetServiceUrl;

    @Override
    public List<Version> getVersions() {
        return singletonList(callVersionService(datasetServiceUrl, "Dataset", VERSION_ENTRY_POINT));
    }

}
