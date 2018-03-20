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

package org.talend.dataprep.transformation.service.export;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.export.ExportParameters;

public class ApplyPreparationExportStrategyTest extends ServiceBaseTest {

    @Autowired
    ApplyPreparationExportStrategy applyPreparationExportStrategy;

    @Test
    public void shouldNotAcceptNullParameter() throws Exception {
        // Then
        assertFalse(applyPreparationExportStrategy.accept(null));
    }

    @Test
    public void shouldNotAcceptIfDataSetParameterNotSet() throws Exception {
        // Given
        final ExportParameters parameters = new ExportParameters();
        parameters.setDatasetId("");
        parameters.setPreparationId("1234");
        parameters.setStepId("0");
        parameters.setExportType("text");
        parameters.setFrom(ExportParameters.SourceType.HEAD);

        // Then
        assertFalse(applyPreparationExportStrategy.accept(parameters));
    }

    @Test
    public void shouldNotAcceptIfPreparationParameterNotSet() throws Exception {

        // Given
        final ExportParameters parameters = new ExportParameters();
        parameters.setDatasetId("1234");
        parameters.setPreparationId("");
        parameters.setStepId("0");
        parameters.setExportType("text");
        parameters.setFrom(ExportParameters.SourceType.HEAD);

        // Then
        assertFalse(applyPreparationExportStrategy.accept(parameters));
    }

    @Test
    public void shouldAcceptIfDataSetAndPreparationParametersSet() throws Exception {
        // Given
        final ExportParameters parameters = new ExportParameters();
        parameters.setDatasetId("1234");
        parameters.setPreparationId("1234");

        // Then
        assertTrue(applyPreparationExportStrategy.accept(parameters));

    }

}
