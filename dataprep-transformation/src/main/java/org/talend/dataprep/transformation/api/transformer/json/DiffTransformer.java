//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.transformer.json;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.transformation.BaseTransformer;
import org.talend.dataprep.transformation.api.action.ActionParser;
import org.talend.dataprep.transformation.api.action.ParsedActions;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.transformer.Transformer;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.api.transformer.configuration.PreviewConfiguration;
import org.talend.dataprep.transformation.format.WriterRegistrationService;

/**
 * Transformer that preview the transformation (puts additional json content so that the front can display the
 * difference between current and previous transformation).
 */
@Component
class DiffTransformer implements Transformer {

    @Autowired
    private ActionParser actionParser;

    /** Service who knows about registered writers. */
    @Autowired
    private WriterRegistrationService writersService;

    /**
     * Starts the transformation in preview mode.
     *
     * @param input the dataset content.
     * @param configuration The {@link Configuration configuration} for this transformation.
     */
    @Override
    public void transform(DataSet input, Configuration configuration) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }
        final PreviewConfiguration previewConfiguration = (PreviewConfiguration) configuration;
        final TransformerWriter writer = writersService.getWriter(configuration.formatId(), configuration.output(),
                configuration.getArguments());

        // parse and extract diff configuration
        final ParsedActions referenceActions = actionParser.parse(previewConfiguration.getReferenceActions());
        InternalTransformationContext reference = new InternalTransformationContext(referenceActions,
                previewConfiguration.getReferenceContext());

        final ParsedActions previewActions = actionParser.parse(previewConfiguration.getPreviewActions());
        InternalTransformationContext preview = new InternalTransformationContext(previewActions,
                previewConfiguration.getPreviewContext());

        // extract TDP ids infos
        final List<Long> indexes = previewConfiguration.getIndexes();
        final boolean isIndexLimited = indexes != null && !indexes.isEmpty();
        final Long minIndex = isIndexLimited ? indexes.stream().mapToLong(Long::longValue).min().getAsLong() : 0L;
        final Long maxIndex = isIndexLimited ? indexes.stream().mapToLong(Long::longValue).max().getAsLong() : Long.MAX_VALUE;

        try {
            // Start diff
            writer.startObject();
            // Records
            writer.fieldName("records");
            writer.startArray();
            final Set<RowMetadata> diff = new HashSet<>(1);

            input.getRecords() //
                    .filter(isWithinWantedIndexes(minIndex, maxIndex)) //
                    .map(createClone()) //
                    .map(rows -> { // Apply actions and generate diff
                        try {
                            reference.apply(rows[0]);
                            preview.apply(rows[1]);
                        } finally {
                            // apply(...) calls reuse same context in multiple transformations, freeze actions to ensure
                            // no unwanted modifications.
                            reference.getContext().freezeActionContexts();
                            preview.getContext().freezeActionContexts();
                        }
                        return rows;
                    }) //
                    .filter(shouldWriteDiff(indexes)) //
                    .forEach(rows -> {
                        try {
                            rows[1].getRowMetadata().diff(rows[0].getRowMetadata());
                            diff.add(rows[1].getRowMetadata());
                            rows[1].diff(rows[0]);
                            if (rows[1].shouldWrite()) {
                                writer.write(rows[1]);
                            }
                        } catch (IOException e) {
                            throw new TDPException(TransformationErrorCodes.UNABLE_TRANSFORM_DATASET, e);
                        }
                    });
            writer.endArray();
            // Write metadata diff
            writer.fieldName("metadata");
            writer.startObject();
            writer.fieldName("columns");
            writer.write(diff.iterator().next());
            diff.clear();
            // End diff
            writer.endObject();
            writer.endObject();
            writer.flush();
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TRANSFORM_DATASET, e);
        } finally {
            // cleanup context (to make sure resources are properly closed)
            reference.getContext().cleanup();
            preview.getContext().cleanup();
        }
    }

    @Override
    public boolean accept(Configuration configuration) {
        return PreviewConfiguration.class.isAssignableFrom(configuration.getClass());
    }

    private Predicate<DataSetRow> isWithinWantedIndexes(Long minIndex, Long maxIndex) {
        return row -> row.getTdpId() >= minIndex && row.getTdpId() <= maxIndex;
    }

    private Function<DataSetRow, DataSetRow[]> createClone() {
        return row -> {
            final DataSetRow reference = new DataSetRow(row.getRowMetadata().clone(), row.values());
            reference.setTdpId(row.getTdpId());
            return new DataSetRow[] { reference, row };
        };
    }

    private Predicate<DataSetRow[]> shouldWriteDiff(List<Long> indexes) {
        return rows -> indexes == null || // no wanted index, we process all rows
                indexes.contains(rows[0].getTdpId()) || // row is a wanted diff row
                rows[0].isDeleted() && !rows[1].isDeleted(); // row is deleted but preview is not
    }

    private class InternalTransformationContext {

        private final ParsedActions parsedActions;

        private final TransformationContext context;

        public InternalTransformationContext(ParsedActions parsedActions, TransformationContext context) {
            this.parsedActions = parsedActions;
            this.context = context;
        }

        public TransformationContext getContext() {
            return context;
        }

        public DataSetRow apply(DataSetRow dataSetRow) {
            return BaseTransformer.baseTransform(Stream.of(dataSetRow), parsedActions.getRowTransformers(), context).findFirst().get();
        }

    }

}
