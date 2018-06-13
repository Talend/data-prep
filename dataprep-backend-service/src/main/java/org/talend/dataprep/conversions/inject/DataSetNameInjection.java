package org.talend.dataprep.conversions.inject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.preparation.PreparationListItemDTO;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;

public class DataSetNameInjection implements BiFunction<PreparationDTO, PreparationListItemDTO, PreparationListItemDTO> {

    private final Map<String, String> dataSetIdToName = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public PreparationListItemDTO apply(PreparationDTO dto, PreparationListItemDTO item) {
        if (!dataSetIdToName.containsKey(dto.getDataSetId())) {
            final DataSetGetMetadata getMetadata = applicationContext.getBean(DataSetGetMetadata.class, dto.getDataSetId());
            dataSetIdToName.put(dto.getDataSetId(), getMetadata.execute().getName());
        }
        item.getDataSet().setDataSetName(dataSetIdToName.get(dto.getDataSetId()));
        return item;
    }
}
