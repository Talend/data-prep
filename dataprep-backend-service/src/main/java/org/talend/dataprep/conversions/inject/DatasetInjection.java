package org.talend.dataprep.conversions.inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DatasetDTO;

import java.util.Set;
import java.util.function.BiFunction;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class DatasetInjection {

    public BiFunction<DataSetMetadata, DatasetDTO, DatasetDTO> injectFavorite(Set<String> favoritesDatasets) {
        return (dataSetMetadata, datasetDTO) -> {
            datasetDTO.setFavorite(favoritesDatasets.contains(datasetDTO.getId()));
            return datasetDTO;
        };

    }
}
