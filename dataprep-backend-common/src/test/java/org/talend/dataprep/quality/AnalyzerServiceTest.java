package org.talend.dataprep.quality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;
import org.talend.dataquality.semantic.classifier.custom.UserDefinedClassifier;
import org.talend.dataquality.semantic.index.LuceneIndex;
import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.snapshot.DictionarySnapshot;
import org.talend.dataquality.semantic.snapshot.DictionarySnapshotProvider;

public class AnalyzerServiceTest {

    private AnalyzerService service;

    @Before
    public void setUp() throws Exception {

        LuceneIndex sharedDataDict = Mockito.mock(LuceneIndex.class);
        LuceneIndex customDataDict = Mockito.mock(LuceneIndex.class);

        DictionarySnapshot dictionarySnapshot = Mockito.mock(DictionarySnapshot.class);
        when(dictionarySnapshot.getMetadata()).thenReturn(createMetadata());
        when(dictionarySnapshot.getCustomDataDict()).thenReturn(customDataDict);
        when(dictionarySnapshot.getSharedDataDict()).thenReturn(sharedDataDict);
        when(dictionarySnapshot.getRegexClassifier()).thenReturn(new UserDefinedClassifier());

        DictionarySnapshotProvider dictionarySnapshotProvider = Mockito.mock(DictionarySnapshotProvider.class);
        when(dictionarySnapshotProvider.get()).thenReturn(dictionarySnapshot);

        service = new AnalyzerService(dictionarySnapshotProvider);
    }

    @Test
    public void buildEmptyColumns() throws Exception {
        assertNotNull(service.build(Collections.emptyList(), AnalyzerService.Analysis.FREQUENCY));
    }

    @Test
    public void buildNullColumn() throws Exception {
        assertNotNull(service.build(((ColumnMetadata) null), AnalyzerService.Analysis.FREQUENCY));
    }

    @Test
    public void buildNullAnalysis() throws Exception {
        assertNotNull(service.build(new ColumnMetadata(), (AnalyzerService.Analysis) null));
    }

    @Test
    public void buildAllAnalysis() throws Exception {
        // Given
        final AnalyzerService.Analysis[] allAnalysis = AnalyzerService.Analysis.values();
        final ColumnMetadata column = new ColumnMetadata();
        column.setType(Type.INTEGER.getName());
        column.setName(UUID.randomUUID().toString());
        try (Analyzer<Analyzers.Result> analyzer = service.build(column, allAnalysis)) {
            assertNotNull(analyzer);
            // When
            analyzer.analyze("");

            // Then
            assertEquals(1, analyzer.getResult().size());
            final Analyzers.Result result = analyzer.getResult().get(0);
            for (AnalyzerService.Analysis analysis : allAnalysis) {
                if (analysis == AnalyzerService.Analysis.TYPE) {
                    // Type analysis is automatically disabled when Quality analysis is requested.
                    assertFalse(result.exist(analysis.getResultClass()));
                } else {
                    assertTrue(result.exist(analysis.getResultClass()));
                }
            }
        }
    }

    private Map<String, DQCategory> createMetadata() {
        Map<String, DQCategory> metadata = new HashMap<>();

        DQCategory airportCodeCategory = new DQCategory();
        airportCodeCategory.setId("1");
        airportCodeCategory.setName("AIRPORT_CODE");
        airportCodeCategory.setLabel("Airport code");

        metadata.put("AIRPORT_CODE", airportCodeCategory);
        return metadata;
    }
}
