package org.talend.dataprep.preparation.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.preparation.StepRowMetadata;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.SecurityProxy;

import static org.talend.tql.api.TqlBuilder.eq;
import static org.talend.tql.api.TqlBuilder.in;

/**
 * Utility class to remove all {@link StepRowMetadata} associated to a preparation that uses a given dataset.
 *
 * @see #removePreparationStepRowMetadata(String)
 */
@Component
public class PreparationUpdateListenerUtil {

    /**
     * The preparation repository.
     */
    @Autowired
    private PreparationRepository preparationRepository;

    @Autowired
    private PreparationUtils preparationUtils;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SecurityProxy securityProxy;

    /**
     * Removes all {@link StepRowMetadata} of preparations that use the provided {@link DataSetMetadata} metadata.
     *
     * @param dataSetId The data set id to be used in preparation search (code searches preparations
     * that use this <code>dataSetId</code>).
     */
    public void removePreparationStepRowMetadata(String dataSetId) {
        final DataSetGetMetadata metadataRetriever = applicationContext.getBean(DataSetGetMetadata.class, dataSetId);
        RowMetadata rowMetadata;
        try {
            securityProxy.asTechnicalUser();
            rowMetadata = metadataRetriever.execute().getRowMetadata();
        } finally {
            securityProxy.releaseIdentity();
        }

        preparationRepository
                .list(Preparation.class, eq("dataSetId", dataSetId)) //
                .forEach(preparation -> {
                    // Reset preparation row metadata.
                    preparation.setRowMetadata(rowMetadata);
                    preparationRepository.add(preparation);

                    // Reset step row metadata in preparation's steps.
                    final String[] idToRemove = preparationUtils
                            .listSteps(preparation.getHeadId(), preparationRepository) //
                            .stream() //
                            .filter(s -> !Step.ROOT_STEP.equals(s)) //
                            .filter(s -> s.getRowMetadata() != null) //
                            .map(Step::getRowMetadata) //
                            .toArray(String[]::new);
                    preparationRepository.remove(StepRowMetadata.class, in("id", idToRemove));
                });
    }
}
