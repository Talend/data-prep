package org.talend.dataprep.dataset;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.json.DataSetLocationMapping;

@Component
public class LocalStoreLocationMapping implements DataSetLocationMapping {

    @Override
    public String getLocationType() {
        return LocalStoreLocation.NAME;
    }

    @Override
    public Class<? extends DataSetLocation> getLocationClass() {
        return LocalStoreLocation.class;
    }
}
