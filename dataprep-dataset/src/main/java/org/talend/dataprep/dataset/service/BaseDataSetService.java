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

package org.talend.dataprep.dataset.service;

import static java.util.Comparator.comparingInt;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.DATASET_NAME_ALREADY_USED;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
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

    @Autowired
    protected ApplicationEventMulticaster asyncPublisher;

    /** DataSet metadata builder. */
    @Autowired
    protected DataSetMetadataBuilder metadataBuilder;

    /** Dataset content store. */
    @Autowired
    protected ContentStoreRouter contentStore;

    /** DQ synchronous analyzers. */
    @Autowired
    private List<SynchronousDataSetAnalyzer> synchronousAnalyzers;

    static void assertDataSetMetadata(DataSetMetadata dataSetMetadata, String dataSetId) {
        if (dataSetMetadata == null) {
            throw new TDPException(DataSetErrorCodes.DATASET_DOES_NOT_EXIST,
                    ExceptionContext.build().put("dataSetId", dataSetId));
        }
        if (dataSetMetadata.getLifecycle().isImporting()) {
            // Data set is being imported, this is an error since user should not have an id to a being-created
            // data set (create() operation is a blocking operation).
            final ExceptionContext context = ExceptionContext.build().put("dataSetId", dataSetId); //$NON-NLS-1$
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_SERVE_DATASET_CONTENT, context);
        }
    }

    /**
     * Sort the synchronous analyzers.
     */
    @PostConstruct
    public void initialize() {
        synchronousAnalyzers.sort(comparingInt(SynchronousDataSetAnalyzer::order));
    }

    public void setSynchronousAnalyzers(List<SynchronousDataSetAnalyzer> synchronousAnalyzers) {
        this.synchronousAnalyzers = synchronousAnalyzers;
    }

    /**
     * Make sure the given name is not used by another dataset. If yes, throws a TDPException.
     *
     * @param datasetName the name to check.
     */
    protected void checkIfNameIsAvailable(String datasetName) {
        if (dataSetMetadataRepository.exist("name = '" + datasetName + "'")) {
            final ExceptionContext context = ExceptionContext
                    .build() //
                    .put("name", datasetName);
            // this exception is used for flux control : it is a bad pattern
            // it will be caught by TDPExceptionController and no stacktrace will be displayed
            throw new TDPException(DATASET_NAME_ALREADY_USED, context, false);
        }
    }

    /**
     * Performs the analysis on the given dataset id.
     * 
     * @param datasetId the dataset id.
     * @param analysersToSkip the list of analysers to skip.
     */
    protected final void analyzeDataSet(String datasetId, List<Class<? extends DataSetAnalyzer>> analysersToSkip) {
        // Calls all synchronous analysis first
        for (SynchronousDataSetAnalyzer synchronousDataSetAnalyzer : synchronousAnalyzers) {
            if (analysersToSkip.contains(synchronousDataSetAnalyzer.getClass())) {
                continue;
            }
            LOG.info("Running {}", synchronousDataSetAnalyzer.getClass());
            synchronousDataSetAnalyzer.analyze(datasetId);
            LOG.info("Done running {}", synchronousDataSetAnalyzer.getClass());
        }

        // important log here (TDP-4137)
        final DataSetMetadata metadata = dataSetMetadataRepository.get(datasetId);
        if (metadata != null) {
            LOG.info("New DataSet #{}, name: {}, type: {}, from: {}", metadata.getId(), metadata.getName(),
                    metadata.getContent().getMediaType(), metadata.getLocation().getStoreName());
        } else {
            LOG.error("Dataset #{} does not exist (but was expected to)", datasetId);
        }
    }

}
