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

package org.talend.dataprep.transformation.format;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;

/**
 * Unit test for WriterRegistrationService.
 *
 * @see WriterRegistrationService
 */
public class WriterRegistrationServiceTest extends BaseFormatTest {

    /** The service to test. */
    @Autowired
    private WriterRegistrationService service;

    private OutputStream output = new ByteArrayOutputStream();

    @Test
    public void shouldReturnSimpleWriter() {
        final TransformerWriter jsonWriter = service.getWriter(JsonFormat.JSON, output, Collections.emptyMap());
        Assert.assertTrue(jsonWriter instanceof JsonWriter);
    }

    @Test
    public void shouldReturnWriterWithParameter() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CSVWriterTest.SEPARATOR_PARAM_NAME, "|");
        final TransformerWriter csvWriter = service.getWriter(CSVFormat.CSV, output, parameters);
        Assert.assertTrue(csvWriter instanceof CSVWriter);
    }

    @Test(expected = TDPException.class)
    public void shouldNotReturnAnything() {
        service.getWriter("toto", output, Collections.emptyMap());
    }
}
