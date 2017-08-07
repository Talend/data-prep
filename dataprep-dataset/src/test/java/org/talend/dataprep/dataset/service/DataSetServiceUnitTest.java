// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.dataset.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.dataset.location.locator.DataSetLocatorService;
import org.talend.dataprep.dataset.store.QuotaService;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.exception.TDPException;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for dataset services.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSetServiceUnitTest {

    @InjectMocks
    private DataSetService dataSetService;

    @Mock
    private DataSetMetadataRepository dataSetMetadataRepository;

    @Mock
    private DataSetLocatorService datasetLocator;

    @Mock
    private QuotaService quotaService;

    @Test
    public void testCreateFailsWhenSizeParameterExceedsAvailableStorage() throws Exception {
        final long size = 123789;
        TDPException expectedException = new TDPException();
        doThrow(expectedException).when(quotaService).checkIfAddingSizeExceedsAvailableStorage(size);

        Throwable exceptionThrown = catchThrowable(
                () -> dataSetService.create("Test dataset", null, size, "content-type", mock(InputStream.class)));

        assertThat(exceptionThrown).isEqualTo(expectedException);
    }

}
