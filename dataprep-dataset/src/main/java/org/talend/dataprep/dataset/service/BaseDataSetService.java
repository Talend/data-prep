//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.dataset.service;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.DATASET_NAME_ALREADY_USED;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.dataset.event.DataSetImportedEvent;
import org.talend.dataprep.dataset.service.analysis.DataSetAnalyzer;
import org.talend.dataprep.dataset.service.analysis.synchronous.SynchronousDataSetAnalyzer;
import org.talend.dataprep.dataset.store.content.ContentStoreRouter;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;

public abstract class BaseDataSetService {

    /** This class' logger. */
    private static final Logger LOG = getLogger(BaseDataSetService.class);

    /** Dataset metadata repository. */
    @Autowired
    protected DataSetMetadataRepository dataSetMetadataRepository;

    @Autowired
    protected ApplicationEventPublisher publisher;

    /** DataSet metadata builder. */
    @Autowired
    protected DataSetMetadataBuilder metadataBuilder;

    /** Dataset content store. */
    @Autowired
    protected ContentStoreRouter contentStore;

    /** DQ synchronous analyzers. */
    @Autowired
    private List<SynchronousDataSetAnalyzer> synchronousAnalyzers;

    /**
     * Sort the synchronous analyzers.
     */
    @PostConstruct
    public void initialize() {
        synchronousAnalyzers.sort((analyzer1, analyzer2) -> analyzer1.order() - analyzer2.order());
    }

    static void assertDataSetMetadata(DataSetMetadata dataSetMetadata, String dataSetId) {
        if (dataSetMetadata == null) {
            throw new TDPException(DataSetErrorCodes.DATASET_DOES_NOT_EXIST, ExceptionContext.build().put("id", dataSetId));
        }
        if (dataSetMetadata.getLifecycle().isImporting()) {
            // Data set is being imported, this is an error since user should not have an id to a being-created
            // data set (create() operation is a blocking operation).
            final ExceptionContext context = ExceptionContext.build().put("id", dataSetId); //$NON-NLS-1$
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_SERVE_DATASET_CONTENT, context);
        }
    }

    /**
     * Make sure the given name is not used by another dataset. If yes, throws a TDPException.
     *
     * @param name the name to check.
     */
    protected void checkIfNameIsAvailable(String name) {
        if (dataSetMetadataRepository.exist("name = '" + name + "'")) {
            final ExceptionContext context = ExceptionContext.build() //
                    .put("name", name);
            throw new TDPException(DATASET_NAME_ALREADY_USED, context);
        }
    }

    /**
     * Performs the analysis on the given dataset id.
     *
     * @param id the dataset id.
     * @param performAsyncBackgroundAnalysis true if the asynchronous background analysis should be performed.
     * @param analysersToSkip the list of analysers to skip.
     */
    protected final void analyzeDataSet(String id, boolean performAsyncBackgroundAnalysis,
            List<Class<? extends DataSetAnalyzer>> analysersToSkip) {

        // Calls all synchronous analysis first
        try {
            for (SynchronousDataSetAnalyzer synchronousDataSetAnalyzer : synchronousAnalyzers) {
                if (analysersToSkip.contains(synchronousDataSetAnalyzer.getClass())) {
                    continue;
                }
                LOG.info("Running {}", synchronousDataSetAnalyzer.getClass());
                synchronousDataSetAnalyzer.analyze(id);
                LOG.info("Done running {}", synchronousDataSetAnalyzer.getClass());
            }
        } catch (Exception e) {
            // Clean up content & metadata (don't keep invalid information)
            try {
                final DataSetMetadata metadata = metadataBuilder.metadata().id(id).build();
                contentStore.delete(metadata);
                dataSetMetadataRepository.remove(id);
            } catch (Exception unableToCleanResources) {
                LOG.error("Unable to clean temporary resources for '{}'.", id, unableToCleanResources);
            }
            throw e;
        }

        // important log here (TDP-4137)
        final DataSetMetadata metadata = dataSetMetadataRepository.get(id);
        LOG.info("New DataSet #{}, name: {}, type: {}, from: {}", metadata.getId(), metadata.getName(),
                metadata.getContent().getMediaType(), metadata.getLocation().getStoreName());

        // perform async analysis
        if (performAsyncBackgroundAnalysis) {
            LOG.debug("starting async background analysis");
            publisher.publishEvent(new DataSetImportedEvent(id));
        } else {
            LOG.info("skipping asynchronous background analysis");
        }
    }

}
