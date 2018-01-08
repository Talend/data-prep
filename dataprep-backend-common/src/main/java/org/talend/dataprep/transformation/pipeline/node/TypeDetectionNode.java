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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
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

    public TypeDetectionNode(Predicate<? super ColumnMetadata> filter, StatisticsAdapter adapter,
            Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer) {
        super(filter);
        this.analyzer = analyzer;
        this.adapter = adapter;
        try {
            reservoir = File.createTempFile("TypeDetection", ".zip");
            JsonFactory factory = new JsonFactory();
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
        performColumnFilter(row, metadata);
        store(row, metadata.getColumns());
        analyze(row);
        count++;
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
            // Lazy initialization of the result analyzer
            if (resultAnalyzer == null) {
                resultAnalyzer = analyzer.apply(filteredColumns);
            }
            final String[] values = row.filter(filteredColumns) //
                    .order(rowMetadata.getColumns()) //
                    .toArray(DataSetRow.SKIP_TDP_ID.and(e -> filteredColumnNames.contains(e.getKey())));
            try {
                resultAnalyzer.analyze(values);
            } catch (Exception e) {
                LOGGER.debug("Unable to analyze row '{}'.", Arrays.toString(values), e);
            }
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNode(this);
    }

    @Override
    public Node copyShallow() {
        return new TypeDetectionNode(filter, adapter, analyzer);
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
                // Send stored records to next steps
                final ObjectMapper mapper = new ObjectMapper();
                if (rowMetadata != null && resultAnalyzer != null) {
                    // Adapt row metadata to infer type (adapter takes care of type-forced columns)
                    resultAnalyzer.end();
                    final List<ColumnMetadata> columns = rowMetadata.getColumns();
                    adapter.adapt(columns, resultAnalyzer.getResult(), (Predicate<ColumnMetadata>) filter);
                    resultAnalyzer.close();
                }
                // Continue process
                try (JsonParser parser = mapper.getFactory().createParser(new GZIPInputStream(new FileInputStream(reservoir)))) {
                    final DataSet dataSet = mapper.reader(DataSet.class).readValue(parser);
                    dataSet.getRecords().forEach(r -> {
                        r.setRowMetadata(rowMetadata);
                        link.exec().emit(r, rowMetadata);
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
}
