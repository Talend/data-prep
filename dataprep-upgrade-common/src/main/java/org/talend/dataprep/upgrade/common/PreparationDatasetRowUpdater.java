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

package org.talend.dataprep.upgrade.common;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;

/**
 * Add the initial dataset metadata to the preparation.
 */
@Component
public class PreparationDatasetRowUpdater {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(PreparationDatasetRowUpdater.class);

    /** The preparation repository. */
    @Autowired
    private PreparationRepository preparationRepository;

    /** The dataset metadata repository. */
    @Autowired
    private DataSetMetadataRepository dataSetMetadataRepository;

    /**
     * Update all the preparations.
     */
    public void updatePreparations() {
        updatePreparations(preparationRepository);
    }

    /**
     * Update all the preparations.
     */
    public void updatePreparations(PreparationRepository repository) {
        repository.list(Preparation.class).map(this::addRowMetadata).forEach(repository::add);
    }

    /**
     * Add the row metadata of the dataset to the preparation.
     *
     * @param preparation the preparation to update.
     * @return the updated preparation.
     */
    private Preparation addRowMetadata(Preparation preparation) {
        LOGGER.debug("adding row metadata to preparation {}", preparation);
        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(preparation.getDataSetId());
        if (dataSetMetadata != null) {
            preparation.setRowMetadata(dataSetMetadata.getRowMetadata());
        } else {
            LOGGER.debug("The metadata of dataset {} is null and will not be used to set the metadata of preparation {}.",
                    preparation.getDataSetId(), preparation.getId());
        }
        return preparation;
    }
}
