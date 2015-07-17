package org.talend.dataprep.dataset.service.analysis;

import static java.util.stream.StreamSupport.stream;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.DistributedLock;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.dataset.exception.DataSetErrorCodes;
import org.talend.dataprep.dataset.store.content.ContentStoreRouter;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;
import org.talend.datascience.common.inference.Analyzer;
import org.talend.datascience.common.inference.Analyzers;
import org.talend.datascience.common.inference.semantic.SemanticAnalyzer;
import org.talend.datascience.common.inference.semantic.SemanticType;
import org.talend.datascience.common.inference.type.DataType;
import org.talend.datascience.common.inference.type.DataTypeAnalyzer;

@Component
public class SchemaAnalysis implements SynchronousDataSetAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaAnalysis.class);

    @Autowired
    DataSetMetadataRepository repository;

    @Autowired
    ContentStoreRouter store;

    @Autowired
    Jackson2ObjectMapperBuilder builder;

    @Override
    public void analyze(String dataSetId) {
        if (StringUtils.isEmpty(dataSetId)) {
            throw new IllegalArgumentException("Data set id cannot be null or empty.");
        }
        DistributedLock datasetLock = repository.createDatasetMetadataLock(dataSetId);
        datasetLock.lock();
        try {
            DataSetMetadata metadata = repository.get(dataSetId);
            if (metadata == null) {
                LOGGER.info("Unable to analyze schema of data set #{}: seems to be removed.", dataSetId);
                return;
            }
            // Schema analysis
            try (Stream<DataSetRow> stream = store.stream(metadata)) {
                LOGGER.info("Analyzing schema in dataset #{}...", dataSetId);
                // Configure analyzers
                final URI ddPath = this.getClass().getResource("/luceneIdx/dictionary").toURI(); //$NON-NLS-1$
                final URI kwPath = this.getClass().getResource("/luceneIdx/keyword").toURI(); //$NON-NLS-1$
                final CategoryRecognizerBuilder builder = CategoryRecognizerBuilder.newBuilder() //
                        .ddPath(ddPath) //
                        .kwPath(kwPath) //
                        .setMode(CategoryRecognizerBuilder.Mode.LUCENE);
                final DataTypeAnalyzer dataTypeAnalyzer = new DataTypeAnalyzer();
                final SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder);
                final Analyzer<Analyzers.Result> analyzer = Analyzers.with(dataTypeAnalyzer, semanticAnalyzer);
                // Determine schema for the content (on the 20 first rows).
                stream.limit(20).map(row -> {
                    final Map<String, Object> rowValues = row.values();
                    final List<String> strings = stream(rowValues.values().spliterator(), false) //
                            .map(String::valueOf) //
                            .collect(Collectors.<String> toList());
                    return strings.toArray(new String[strings.size()]);
                }).forEach(analyzer::analyze);
                // Find the best suitable type
                List<Analyzers.Result> columnTypes = analyzer.getResult();
                final Iterator<ColumnMetadata> columns = metadata.getRow().getColumns().iterator();
                columnTypes.forEach(columnResult -> {
                    // Column data type
                    final DataType dataType = columnResult.get(DataType.class);
                    final Map<DataType.Type, Long> frequencies = dataType.getTypeFrequencies();
                    frequencies.remove(DataType.Type.EMPTY); // TDP-226: Don't take into account EMPTY values.
                    // Look at type frequencies distribution (if not spread enough, fall back to STRING).
                    StandardDeviation standardDeviation = new StandardDeviation();
                    double[] values = new double[frequencies.size()];
                    int i = 0;
                    for (Long frequency : frequencies.values()) {
                        values[i++] = frequency;
                    }
                    final double stdDev = standardDeviation.evaluate(values);
                    final Type type;
                    if (stdDev < 1 && frequencies.size() > 1) {
                        type = Type.STRING;
                    } else {
                        final DataType.Type suggestedType = dataType.getSuggestedType();
                        switch (suggestedType) {
                            case CHAR:
                                // Consider DQ's char type as string in data prep
                                type = Type.STRING;
                                break;
                            default:
                                type = Type.get(suggestedType.name());
                        }
                    }
                    // Semantic type
                    final SemanticType semanticType = columnResult.get(SemanticType.class);
                    if (columns.hasNext()) {
                        final ColumnMetadata nextColumn = columns.next();
                        LOGGER.debug("Column {} -> {}", nextColumn.getId(), type.getName());
                        nextColumn.setType(type.getName());
                        nextColumn.setDomain(semanticType.getSuggestedCategory());
                    } else {
                        LOGGER.error("Unable to set type '" + type.getName() + "' to next column (no more column in dataset).");
                    }
                });
                LOGGER.info("Analyzed schema in dataset #{}.", dataSetId);
                metadata.getLifecycle().schemaAnalyzed(true);
                repository.add(metadata);
            } catch (Exception e) {
                LOGGER.error("Unable to analyse schema for dataset " + dataSetId + ".", e);
                throw new TDPException(DataSetErrorCodes.UNABLE_TO_ANALYZE_COLUMN_TYPES, e);
            }
        } finally {
            datasetLock.unlock();
        }
    }

    @Override
    public int order() {
        return 1;
    }
}