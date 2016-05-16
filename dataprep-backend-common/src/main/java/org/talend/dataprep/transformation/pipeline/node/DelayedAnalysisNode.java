package org.talend.dataprep.transformation.pipeline.node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataprep.util.FilesHelper;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DelayedAnalysisNode extends AnalysisNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelayedAnalysisNode.class);

    private final JsonGenerator generator;

    private final File transformationDelayed;

    private RowMetadata rowMetadata;

    private long totalTime;

    private long count;

    public DelayedAnalysisNode(Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer,
            Predicate<ColumnMetadata> filter, StatisticsAdapter adapter) {
        super(analyzer, filter, adapter);
        try {
            transformationDelayed = File.createTempFile("DelayedAnalysisNode", ".zip");
            JsonFactory factory = new JsonFactory();
            generator = factory.createGenerator(new GZIPOutputStream(new FileOutputStream(transformationDelayed), true));
            generator.writeStartObject();
            generator.writeFieldName("records");
            generator.writeStartArray();
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        final long start = System.currentTimeMillis();
        try {
            if (!row.isDeleted()) {
                final List<ColumnMetadata> columns = metadata.getColumns();
                try {
                    generator.writeStartObject();
                    rowMetadata = metadata;
                    columns.stream().filter(filter).forEach(column -> {
                        try {
                            generator.writeStringField(column.getId(), row.get(column.getId()));
                        } catch (IOException e) {
                            LOGGER.debug("Unable to keep values for delayed analysis.", e);
                        }
                    });
                    generator.writeEndObject();
                } catch (IOException e) {
                    LOGGER.debug("Unable to keep values for delayed analysis.", e);
                }
            }
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }

        link.exec().emit(row, metadata);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitDelayedAnalysis(this);
    }

    @Override
    public void signal(Signal signal) {
        final long start = System.currentTimeMillis();
        try {
            if (signal == Signal.END_OF_STREAM || signal == Signal.CANCEL) {
                // End temporary output
                generator.writeEndArray();
                generator.writeEndObject();
                generator.flush();
                // Prepare columns for analysis
                if (rowMetadata != null) {
                    final List<ColumnMetadata> columns = rowMetadata.getColumns().stream().filter(filter)
                            .collect(Collectors.toList());
                    LOGGER.debug("Reduced column analysis from {} to {}.", rowMetadata.getColumns().size(), columns.size());
                    LOGGER.debug("Column types: {}.", columns.stream().map(ColumnMetadata::getType).collect(Collectors.toList()));
                    try (final Analyzer<Analyzers.Result> delayedAnalyzer = analyzer.apply(columns)) {
                        // Process it
                        ObjectMapper mapper = new ObjectMapper();
                        try (JsonParser parser = mapper.getFactory()
                                .createParser(new GZIPInputStream(new FileInputStream(transformationDelayed)))) {
                            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
                            final Set<String> columnToAnalyze = columns.stream().map(ColumnMetadata::getId)
                                    .collect(Collectors.toSet());
                            dataSet.getRecords().forEach(r -> {
                                final String[] values = r.order(rowMetadata.getColumns()) //
                                        .toArray(DataSetRow.SKIP_TDP_ID.and(e -> columnToAnalyze.contains(e.getKey())));
                                delayedAnalyzer.analyze(values);
                            });
                        }
                        delayedAnalyzer.end();
                        // Adapt results
                        List<String> forcedTypes = columns.stream().map(c -> {
                            if (c.isTypeForced()) {
                                return c.getType();
                            } else {
                                return StringUtils.EMPTY;
                            }
                        }).collect(Collectors.toList());
                        for (ColumnMetadata column : columns) {
                            // temporary type forced disabled (analyzer was configured with forced type), but other
                            column.setTypeForced(false);
                        }
                        adapter.adapt(columns, delayedAnalyzer.getResult(), filter);
                        final Iterator<String> iterator = forcedTypes.iterator();
                        for (ColumnMetadata column : columns) {
                            final String forcedType = iterator.next();
                            if (!forcedType.isEmpty()) {
                                column.setTypeForced(true);
                                column.setType(forcedType);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to perform delayed analysis.", e);
        } finally {
            FilesHelper.deleteQuietly(transformationDelayed);
            totalTime += System.currentTimeMillis() - start;
        }
        super.signal(signal);
    }

    @Override
    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public long getCount() {
        return count;
    }
}
