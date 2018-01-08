//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.dataset.service.analysis;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.dataset.DataSetBaseTest;
import org.talend.dataprep.dataset.service.analysis.synchronous.SynchronousDataSetAnalyzer;

public class DataSetAnalyzersTest extends DataSetBaseTest {

    @Autowired
    List<DataSetAnalyzer> allAnalyzers;

    @Autowired
    List<SynchronousDataSetAnalyzer> synchronousAnalyzers;

    @Test
    public void testSynchronousOrder() throws Exception {
        synchronousAnalyzers.sort((analyzer1, analyzer2) -> analyzer1.order() - analyzer2.order());
        int previousOrder = -1;
        for (SynchronousDataSetAnalyzer synchronousAnalyzer : synchronousAnalyzers) {
            assertThat(synchronousAnalyzer.order(), greaterThan(previousOrder));
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
