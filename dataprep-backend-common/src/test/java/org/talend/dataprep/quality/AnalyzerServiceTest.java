package org.talend.dataprep.quality;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

public class AnalyzerServiceTest {

    private AnalyzerService service;

    @Before
    public void setUp() throws Exception {
        service = new AnalyzerService();
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
}
