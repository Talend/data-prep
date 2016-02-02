//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.schema.csv;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.SchemaParser;

/**
 * Unit test for CSVSchemaUpdater.
 *
 * @see CSVSchemaUpdater
 */
public class CSVSchemaUpdaterTest extends AbstractSchemaTestUtils {

    @InjectMocks
    private CSVSchemaUpdater updater;

    @Mock
    private CSVFormatUtils csvFormatUtils;
    @Mock
    private CSVFormatGuesser csvFormatGuesser;

    @Autowired
    private DataSetMetadataBuilder metadataBuilder;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldAccept() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("toto").formatGuessId("formatGuess#csv").build();
        assertTrue(updater.accept(metadata));
    }

    @Test
    public void shouldNoAccept() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("tata").formatGuessId("formatGuess#xls").build();
        assertFalse(updater.accept(metadata));
    }

    @Test
    public void shouldCallCsvFormatUtils() throws Exception {
        final DataSetMetadata updated = metadataBuilder.metadata().id("toto").encoding("UTF-8").build();
        final SchemaParser.Request request = new SchemaParser.Request(new ByteArrayInputStream(new byte[]{}), updated);
        updater.updateSchema(request);

        when(csvFormatGuesser.guess(request, updated.getEncoding())).thenReturn(null);
        verify(csvFormatUtils, times(1)).useNewSeparator(updated);
        verify(csvFormatGuesser, times(1)).guess(request, updated.getEncoding());
    }
}