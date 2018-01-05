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

package org.talend.dataprep.actions;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.PreparationParser;
import org.talend.dataprep.StandalonePreparation;
import org.talend.dataprep.actions.resources.DictionaryResource;
import org.talend.dataprep.actions.resources.FunctionResource;
import org.talend.dataprep.actions.resources.FunctionResourceProvider;
import org.talend.dataprep.actions.resources.LookupResource;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.api.filter.PolyglotFilterService;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.Pipeline;

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
        StandalonePreparation minimalPreparation = PreparationParser.parsePreparation(preparation);
        return create(minimalPreparation, resources);
    }

    public Function<IndexedRecord, IndexedRecord> create(final InputStream preparation, FunctionResourceProvider... providers) {
        StandalonePreparation minimalPreparation = PreparationParser.parsePreparation(preparation);
        return create(minimalPreparation, providers);
    }

    public Function<IndexedRecord, IndexedRecord> create(StandalonePreparation preparation,
            FunctionResourceProvider... providers) {
        if (preparation.getActions() == null) {
            LOGGER.info("No action defined in preparation, returning identity function");
            return new NoOpFunction();
        }

        List<RunnableAction> actions = PreparationParser.ensureActionRowsExistence(preparation.getActions(),
                allowNonDistributedActions);

        // get the list of resources for function
        FunctionResource[] resources = Arrays.stream(providers) //
                .map(provider -> provider.get(actions)) //
                .collect(Collectors.toList()) //
                .toArray(new FunctionResource[providers.length]);

        return create(preparation, resources);
    }

    public Function<IndexedRecord, IndexedRecord> create(StandalonePreparation preparation, FunctionResource... resources) {
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
                .withFilterOut(new FilterOutProvider(preparation)) //
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
        StandalonePreparation standalonePreparation = PreparationParser.parsePreparation(inputStream);
        FunctionResource lookup = new LookupResource(standalonePreparation.getLookupDataSets());
        FunctionResource dictionary = new DictionaryResource(standalonePreparation.getTdqCategories());
        return create(standalonePreparation, lookup, dictionary);
    }

    // Can't use identity() because result isn't serializable
    static class NoOpFunction implements Function<IndexedRecord, IndexedRecord>, Serializable {

        @Override
        public IndexedRecord apply(IndexedRecord indexedRecord) {
            return indexedRecord;
        }
    }

    static class FilterOutProvider implements Function<RowMetadata, Predicate<DataSetRow>>, Serializable {

        private final StandalonePreparation preparation;

        FilterOutProvider(StandalonePreparation preparation) {
            this.preparation = preparation;
        }

        @Override
        public Predicate<DataSetRow> apply(RowMetadata metadata) {
            final FilterService filterService = Providers.get(PolyglotFilterService.class);
            return filterService.build(preparation.getFilterOut(), metadata);
        }
    }

}
