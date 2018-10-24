package org.talend.dataprep.conversions.inject;

import java.util.Set;
import java.util.function.BiFunction;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DatasetDTO;

@Component
public class DatasetInjection {

    public BiFunction<DataSetMetadata, DatasetDTO, DatasetDTO> injectFavorite(Set<String> favoritesDatasets) {
        return (dataSetMetadata, datasetDTO) -> {
            datasetDTO.setFavorite(favoritesDatasets.contains(datasetDTO.getId()));
            return datasetDTO;
        };

    }
}
