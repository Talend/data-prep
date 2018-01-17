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

package org.talend.dataprep.transformation.format;

import static org.junit.Assert.assertThat;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.format.export.ExportFormatMessage;
import org.talend.dataprep.test.SpringLocalizationRule;
import org.talend.dataprep.transformation.TransformationBaseTest;

/**
 * Base class for all format tests.
 */
public abstract class BaseFormatTest extends TransformationBaseTest {

    /** Spring application context. */
    @Autowired
    protected ApplicationContext context;

    @Autowired
    protected BeanConversionService beanConversionService;

    @Rule
    public SpringLocalizationRule rule = new SpringLocalizationRule(Locale.US);

    protected void testFormat(ExportFormat format, String expectedJson) throws IOException {
        StringWriter writer = new StringWriter();
        final ExportFormatMessage exportFormatMessage = beanConversionService.convert(format, ExportFormatMessage.class);
        mapper.writer().writeValue(writer, exportFormatMessage);
        assertThat(writer.toString(), sameJSONAsFile(BaseFormatTest.class.getResourceAsStream(expectedJson)));
    }

}
