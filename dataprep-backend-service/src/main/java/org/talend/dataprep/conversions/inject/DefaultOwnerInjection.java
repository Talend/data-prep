package org.talend.dataprep.conversions.inject;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DatasetDTO;
import org.talend.dataprep.api.dataset.DatasetDetailsDTO;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.security.Security;

import javax.annotation.PostConstruct;
import java.util.function.BiFunction;

/**
 * A default implementation of {@link OwnerInjection} dedicated to environments with no security enabled.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class DefaultOwnerInjection implements OwnerInjection {

    @Autowired
    private Security security;

    private Owner defaultOwner;

    @PostConstruct
    public void init() {
        defaultOwner = new Owner(security.getUserId(), security.getUserDisplayName(), StringUtils.EMPTY);
    }

    @Override
    public BiFunction<PersistentPreparation, PreparationDTO, PreparationDTO> injectIntoPreparation() {
        return (persistentPreparation, prepDTO) -> {
            if (prepDTO.getOwner() == null) {
                prepDTO.setOwner(defaultOwner);
            }
            return prepDTO;
        };
    }

    @Override
    public BiFunction<Dataset, DatasetDTO, DatasetDTO> injectIntoDataset() {
        return (dataset, datasetDTO) -> {
            if (datasetDTO.getOwner() == null) {
                datasetDTO.setOwner(defaultOwner);
            }
            return datasetDTO;
        };
    }

    @Override
    public BiFunction<Dataset, DatasetDetailsDTO, DatasetDetailsDTO> injectIntoDatasetDetails() {
        return (dataset, datasetDetailsDTO) -> {
            if (datasetDetailsDTO.getOwner() == null) {
                datasetDetailsDTO.setOwner(defaultOwner);
            }
            return datasetDetailsDTO;
        };
    }
}
