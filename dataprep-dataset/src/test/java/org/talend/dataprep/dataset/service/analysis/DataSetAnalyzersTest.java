package org.talend.dataprep.dataset.service.analysis;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.dataset.DataSetBaseTest;

public class DataSetAnalyzersTest extends DataSetBaseTest {

    @Autowired
    List<DataSetAnalyzer> allAnalyzers;

    @Autowired
    List<SynchronousDataSetAnalyzer> synchronousAnalyzers;

    @Autowired
    List<AsynchronousDataSetAnalyzer> asynchronousAnalyzers;

    @Test
    public void testSynchronousOrder() throws Exception {
        synchronousAnalyzers.sort((analyzer1, analyzer2) -> analyzer1.order() - analyzer2.order());
        int previousOrder = -1;
        for (SynchronousDataSetAnalyzer synchronousAnalyzer : synchronousAnalyzers) {
            assertThat(synchronousAnalyzer.order(), greaterThan(previousOrder));
        }
    }

    @Test
    public void testAsynchronousDestinations() throws Exception {
        for (AsynchronousDataSetAnalyzer asynchronousAnalyzer : asynchronousAnalyzers) {
            assertThat(asynchronousAnalyzer.destination(), notNullValue());
            assertThat(asynchronousAnalyzer.destination(), not(is("")));
        }
    }

    @Test
    public void testArguments() throws Exception {
        for (DataSetAnalyzer analyzer : allAnalyzers) {
            try {
                analyzer.analyze(null);
                fail();
            } catch (IllegalArgumentException e) {
                // Expected
            }
            try {
                analyzer.analyze("");
                fail();
            } catch (IllegalArgumentException e) {
                // Expected
            }
        }
    }

}
