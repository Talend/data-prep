// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.actions;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.StandalonePreparation;
import org.talend.dataprep.PreparationParser;
import org.talend.dataprep.actions.resources.DictionaryResource;
import org.talend.dataprep.actions.resources.FunctionResource;
import org.talend.dataprep.actions.resources.FunctionResourceProvider;
import org.talend.dataprep.actions.resources.LookupResource;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.Pipeline;
import org.talend.dataprep.transformation.service.Dictionaries;
import org.talend.dataquality.semantic.broadcast.BroadcastIndexObject;

/**
 * A factory creating a {@link SerializableFunction} from provided arguments.
 */
public class StandalonePreparationFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandalonePreparationFactory.class);

    /**
     * A flag to know whether non distributed actions are allowed.
     */
    private boolean allowNonDistributedActions = false;

    public void setAllowNonDistributedActions(boolean allowNonDistributedActions) {
        this.allowNonDistributedActions = allowNonDistributedActions;
    }

    /**
     * Returns a {@link SerializableFunction} used to apply the preparation to an
     * {@link IndexedRecord}.
     *
     * @param preparation the preparation to apply to {@link SerializableFunction}.
     * //* @param lookupDataSets the data sets that are used in lookup actions if any
     * // * @param dictionary the dictionary used to apply the preparation
     * @return a {@link SerializableFunction} used to apply the preparation to an
     * {@link IndexedRecord}
     */
    public Function<IndexedRecord, IndexedRecord> create(final InputStream preparation, FunctionResource... resources) {
        PreparationMessage minimalPreparation = PreparationParser.parseCorePreparation(preparation);
        return create(minimalPreparation, resources);
    }

    public Function<IndexedRecord, IndexedRecord> create(final InputStream preparation, FunctionResourceProvider... providers) {
        PreparationMessage minimalPreparation = PreparationParser.parseCorePreparation(preparation);
        return create(minimalPreparation, providers);
    }

    public Function<IndexedRecord, IndexedRecord> create(PreparationMessage preparation, FunctionResourceProvider... providers) {
        if (preparation.getActions() == null) {
            LOGGER.info("No action defined in preparation, returning identity function");
            return new NoOpFunction();
        }

        List<RunnableAction> actions = PreparationParser.ensureActionRowsExistence(preparation.getActions(), allowNonDistributedActions);

        // get the list of resources for function
        FunctionResource[] resources = Arrays.stream(providers) //
                .map(provider -> provider.get(actions)) //
                .collect(Collectors.toList()) //
                .toArray(new FunctionResource[providers.length]);

        return create(preparation, resources);
    }

    public Function<IndexedRecord, IndexedRecord> create(PreparationMessage preparation, FunctionResource... resources) {
        if (preparation.getActions() == null) {
            LOGGER.info("No action defined in preparation, returning identity function");
            return new NoOpFunction();
        }

        RowMetadata rowMetadata = preparation.getRowMetadata();

        List<RunnableAction> actions = PreparationParser.ensureActionRowsExistence(preparation.getActions(), allowNonDistributedActions);

        LOGGER.trace("The initial row metadata is: " + rowMetadata);

        // Build internal transformation pipeline
        final StackedNode stackedNode = new StackedNode();
        final Pipeline pipeline = Pipeline.Builder.builder() //
                .withActionRegistry(PreparationParser.actionRegistry) //
                .withActions(actions) //
                .withInitialMetadata(rowMetadata, true) //
                .withOutput(() -> stackedNode) //
                .withStatisticsAdapter(new StatisticsAdapter(40)) //
                .withGlobalStatistics(false) //
                .allowMetadataChange(false) //
                .withAnalyzerService(Providers.get(AnalyzerService.class)) //
                .build();

        return new SerializableFunction(pipeline, stackedNode, rowMetadata, resources);
    }

    /**
     * Returns a {@link SerializableFunction} used to apply the preparation to an
     * {@link IndexedRecord}.
     * 
     * @param inputStream a JSON object containing the preparation, the data sets that are used in lookup actions if
     * any, the dictionary used to apply the preparation.
     * @return
     */
    public Function<IndexedRecord, IndexedRecord> create(final InputStream inputStream) {
        StandalonePreparation standalonePreparation = PreparationParser.parseExportableCorePreparation(inputStream);
        FunctionResource lookup = new LookupResource(standalonePreparation.getLookupDataSets());
        BroadcastIndexObject dictionaryIndexObject = new BroadcastIndexObject(standalonePreparation.getDictionary());
        BroadcastIndexObject keywordIndexObject = new BroadcastIndexObject(standalonePreparation.getKeyword());
        FunctionResource dictionary = new DictionaryResource(new Dictionaries(dictionaryIndexObject, keywordIndexObject));
        return create(standalonePreparation, lookup, dictionary);
    }

    // Can't use identity() because result isn't serializable
    static class NoOpFunction implements Function<IndexedRecord, IndexedRecord>, Serializable {

        @Override
        public IndexedRecord apply(IndexedRecord indexedRecord) {
            return indexedRecord;
        }
    }

}
