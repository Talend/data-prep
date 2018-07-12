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

package org.talend.dataprep.transformation.pipeline.node;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.FlagNames;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataprep.util.FilesHelper;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TypeDetectionNode extends ColumnFilteredNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeDetectionNode.class);

    private final JsonGenerator generator;

    private final File reservoir;

    private final Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer;

    private final StatisticsAdapter adapter;

    private long totalTime;

    private Analyzer<Analyzers.Result> resultAnalyzer;

    private long count;

    private RowMetadata metadata;

    public TypeDetectionNode(RowMetadata initialRowMetadata, //
            Predicate<String> filter, //
            StatisticsAdapter adapter, //
            Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer) {
        super(filter, initialRowMetadata);
        this.analyzer = analyzer;
        this.adapter = adapter;
        try {
            reservoir = File.createTempFile("TypeDetection", ".zip");
            final JsonFactory factory = new JsonFactory();
            factory.configure(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM, false);
            generator = factory.createGenerator(new GZIPOutputStream(new FileOutputStream(reservoir), true));
            generator.writeStartObject();
            generator.writeFieldName("records");
            generator.writeStartArray();
        } catch (IOException e) {
            throw new TalendRuntimeException(BaseErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        this.metadata = metadata;
        final long start = System.currentTimeMillis();
        try {
            store(row, metadata.getColumns());
            analyze(row);
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
    }

    // Store row in temporary file
    private void store(DataSetRow row, List<ColumnMetadata> columns) {
        try {
            generator.writeStartObject();
            columns.forEach(column -> {
                try {
                    generator.writeStringField(column.getId(), row.get(column.getId()));
                } catch (IOException e) {
                    throw new TalendRuntimeException(BaseErrorCodes.UNEXPECTED_EXCEPTION, e);
                }
            });
            if (row.isDeleted()) {
                generator.writeBooleanField("_deleted", true);
            }
            final Optional<Long> tdpId = Optional.ofNullable(row.getTdpId());
            if (tdpId.isPresent()) {
                generator.writeNumberField(FlagNames.TDP_ID, tdpId.get());
            }
            for (Map.Entry<String, String> entry : row.getInternalValues().entrySet()) {
                generator.writeStringField(entry.getKey(), entry.getValue());
            }
            generator.writeEndObject();
        } catch (IOException e) {
            throw new TalendRuntimeException(BaseErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    // Analyze row using lazily configured analyzer
    private void analyze(DataSetRow row) {
        if (!row.isDeleted()) {
            final List<ColumnMetadata> filteredColumns = getFilteredColumns().collect(Collectors.toList());
            // Lazy initialization of the result analyzer
            if (resultAnalyzer == null) {
                resultAnalyzer = analyzer.apply(filteredColumns);
            }
            final String[] values = row
                    .filter(filteredColumns) //
                    .order(metadata.getColumns()) //
                    .toArray(DataSetRow.SKIP_TDP_ID.and(e -> filter.test(e.getKey())));
            try {
                resultAnalyzer.analyze(values);
            } catch (Exception e) {
                LOGGER.debug("Unable to analyze row '{}'.", Arrays.toString(values), e);
            }
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitTypeDetection(this);
    }

    @Override
    public Node copyShallow() {
        return new TypeDetectionNode(initialRowMetadata, filter, adapter, analyzer);
    }

    @Override
    public void signal(Signal signal) {

        final long start = System.currentTimeMillis();
        try {
            if (signal == Signal.END_OF_STREAM || signal == Signal.CANCEL || signal == Signal.STOP) {
                // End temporary output
                generator.writeEndArray();
                generator.writeEndObject();
                generator.flush();
                generator.close();
                // Send stored records to next steps
                final ObjectMapper mapper = new ObjectMapper();
                if (metadata != null && resultAnalyzer != null) {
                    // Adapt row metadata to infer type (adapter takes care of type-forced columns)
                    resultAnalyzer.end();
                    final List<ColumnMetadata> columns = metadata.getColumns();
                    adapter.adapt(columns, resultAnalyzer.getResult(), filter);
                    resultAnalyzer.close();
                }
                // Continue process
                try (JsonParser parser = mapper.getFactory().createParser(
                        new InputStreamReader(new GZIPInputStream(new FileInputStream(reservoir)), UTF_8))) {
                    final DataSet dataSet = mapper.reader(DataSet.class).readValue(parser);
                    dataSet.getRecords().forEach(r -> {
                        r.setRowMetadata(metadata);
                        link.exec().emit(r, metadata);
                    });

                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to perform delayed analysis.", e);
        } finally {
            try {
                generator.close();
            } catch (IOException e) {
                LOGGER.error("Unable to close JSON generator (causing potential temp file delete issues).", e);
            }
            FilesHelper.deleteQuietly(reservoir);
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

    @Override
    public List<String> getColumnNames() {
        return getFilteredColumns() //
                .map(ColumnMetadata::getId) //
                .collect(Collectors.toList());
    }

    private Stream<ColumnMetadata> getFilteredColumns() {
        return initialRowMetadata
                .getColumns()
                .stream() //
                .filter(c -> filter.test(c.getId()) && !c.isTypeForced());
    }
}
