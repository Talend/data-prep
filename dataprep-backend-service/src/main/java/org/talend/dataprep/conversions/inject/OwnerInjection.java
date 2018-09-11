package org.talend.dataprep.conversions.inject;

import org.talend.dataprep.api.dataset.DatasetDTO;
import org.talend.dataprep.api.dataset.DatasetDetailsDTO;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.preparation.store.PersistentPreparation;

import java.util.function.BiFunction;

/**
 * An API to ease {@link org.talend.dataprep.api.share.Owner} injection in {@link PreparationDTO} instances.
 *
 * @see org.talend.dataprep.conversions.BeanConversionService#convert(Object, Class, BiFunction[])
 */
public interface OwnerInjection {

    BiFunction<PersistentPreparation, PreparationDTO, PreparationDTO> injectIntoPreparation();

    BiFunction<Dataset, DatasetDTO, DatasetDTO> injectIntoDataset();

    BiFunction<Dataset, DatasetDetailsDTO, DatasetDetailsDTO> injectIntoDatasetDetails();
}
