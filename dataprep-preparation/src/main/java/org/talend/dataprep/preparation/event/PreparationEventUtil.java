package org.talend.dataprep.preparation.event;

import static org.talend.tql.api.TqlBuilder.eq;
import static org.talend.tql.api.TqlBuilder.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.preparation.StepRowMetadata;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.cache.TransformationCacheKey;
import org.talend.dataprep.dataset.adapter.DatasetClient;
import org.talend.dataprep.event.CacheEventProcessingUtil;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.SecurityProxy;

/**
 * Utility class to remove all {@link StepRowMetadata} associated to a preparation that uses a given dataset.
 *
 * @see #updatesFromDataSetMetadata(String)
 */
@Component
public class PreparationEventUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationEventUtil.class);

    /**
     * The preparation repository.
     */
    @Autowired
    private PreparationRepository preparationRepository;

    @Autowired
    private PreparationUtils preparationUtils;

    @Autowired
    private DatasetClient datasetClient;

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    @Autowired
    private CacheEventProcessingUtil cacheEventProcessingUtil;

    @Autowired
    private SecurityProxy securityProxy;

    public void performUpdateEvent(String datasetId) {
        LOGGER.info("Performing update event for dataset {}", datasetId);
        cleanTransformationCache(datasetId);
        cleanTransformationMetadataCache(datasetId);
        updatesFromDataSetMetadata(datasetId);
    }

    private void cleanTransformationCache(String datasetId) {
        LOGGER.info("Evicting transformation cache entry for dataset #{}", datasetId);
        TransformationCacheKey transformationCacheKey =
                cacheKeyGenerator.generateContentKey(datasetId, null, null, null, null, null);
        cacheEventProcessingUtil.processCleanCacheEvent(transformationCacheKey, Boolean.TRUE);
        LOGGER.debug("Evicting transformation cache entry for dataset #{} done.", datasetId);
    }

    private void cleanTransformationMetadataCache(String datasetId) {
        LOGGER.info("Evicting transformation metadata cache entry for dataset #{}", datasetId);
        try {
            securityProxy.asTechnicalUser();
            preparationRepository
                    .list(PersistentPreparation.class, eq("dataSetId", datasetId)) //
                    .forEach(preparation -> {
                        ContentCacheKey metadataKey =
                                cacheKeyGenerator.generateMetadataKey(preparation.getId(), null, null);
                        cacheEventProcessingUtil.processCleanCacheEvent(metadataKey, Boolean.TRUE);
                    });
        } finally {
            securityProxy.releaseIdentity();
        }
        LOGGER.debug("Evicting transformation metadata cache entry for dataset #{} done.", datasetId);
    }

    /**
     * Perform all {@link DataSetMetadata} related operations:
     * <ul>
     * <li>Removes all {@link StepRowMetadata} of preparations that use the provided {@link DataSetMetadata}
     * metadata.</li>
     * <li>Update preparation's data set name.</li>
     * </ul>
     * Do all operations in <b>one</b> method to prevent multiple lookup for a given dataset.
     *
     * @param dataSetId The data set id to be used in preparation search (code searches preparations that use this
     * <code>dataSetId</code>).
     */
    private void updatesFromDataSetMetadata(String dataSetId) {
        LOGGER.info("Updating metadata for dataset #{}", dataSetId);
        DataSetMetadata dataSetMetadata;
        try {
            securityProxy.asTechnicalUserForDataSet();
            dataSetMetadata = datasetClient.getDataSetMetadata(dataSetId);
        } finally {
            securityProxy.releaseIdentity();
        }
        if (dataSetMetadata == null) {
            LOGGER.error("Unable to clean step row metadata of preparations using dataset '{}' (dataset not found).",
                    dataSetId);
            return;
        }
        final RowMetadata rowMetadata = dataSetMetadata.getRowMetadata();
        try {
            securityProxy.asTechnicalUser();
            preparationRepository
                    .list(PersistentPreparation.class, eq("dataSetId", dataSetId)) //
                    .forEach(preparation -> {
                        preparation.setDataSetName(dataSetMetadata.getName());
                        preparation.setRowMetadata(rowMetadata);
                        preparationRepository.add(preparation);

                        // Reset step row metadata in preparation's steps.
                        final String[] idToRemove =
                                preparationUtils
                                        .listSteps(preparation.getHeadId(), preparationRepository) //
                                        .stream() //
                                        .filter(s -> !Step.ROOT_STEP.equals(s)) //
                                        .filter(s -> s.getRowMetadata() != null) //
                                        .map(Step::getRowMetadata) //
                                        .toArray(String[]::new);
                        preparationRepository.remove(StepRowMetadata.class, in("id", idToRemove));
                    });
        } finally {
            securityProxy.releaseIdentity();
        }
        LOGGER.debug("Updating metadata for dataset #{} done", dataSetId);
    }
}
