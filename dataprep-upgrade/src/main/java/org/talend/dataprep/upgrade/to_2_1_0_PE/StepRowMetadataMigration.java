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

package org.talend.dataprep.upgrade.to_2_1_0_PE;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.StepRowMetadata;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTask;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thanks to TDP-3943 the classes saved in mongodb changed. The row metadata needs to be stored in a separate
 * identifiable object.
 */
@Component
public class StepRowMetadataMigration implements BaseUpgradeTaskTo_2_1_0_PE {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(StepRowMetadataMigration.class);

    @Autowired
    private PreparationRepository preparationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run() {
        // Allow non numeric value like NaN
        objectMapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        LOGGER.info("Migration of step row metadata in preparations...");

        preparationRepository.list(PersistentStep.class).forEach(persistentStep -> {
            String id = persistentStep.getId();
            LOGGER.info("Migration of step #{}", id);
            String rowMetadata = persistentStep.getRowMetadata();
            try {
                // the rootstep has no metadata => avoid conversion
                if (rowMetadata != null) {
                    // Dirty patch to convert all histogram (2.0) to new one (2.1)
                    rowMetadata = rowMetadata.replace("_class", "type")
                            .replace("org.talend.dataprep.api.dataset.statistics.number.NumberHistogram", "number")
                            .replace("org.talend.dataprep.api.dataset.statistics.date.DateHistogram", "date");
                    final DataSetMetadata dataSetMetadata = objectMapper.readerFor(DataSetMetadata.class).readValue(rowMetadata);
                    final StepRowMetadata stepRowMetadata = new StepRowMetadata(dataSetMetadata.getRowMetadata());

                    persistentStep.setRowMetadata(stepRowMetadata.getId());

                    preparationRepository.add(persistentStep);
                    preparationRepository.add(stepRowMetadata);
                }
            } catch (Exception e) {
                LOGGER.info("Ignore migration of step #{} (enable debug for full log).", id);
                LOGGER.debug("Unable to migrate step", e);
            }

        });
        LOGGER.info("Migration of step metadata in preparations done.");

    }

    @Override
    public int getOrder() {
        return 5;
    }

    @Override
    public UpgradeTask.target getTarget() {
        return VERSION;
    }

}
