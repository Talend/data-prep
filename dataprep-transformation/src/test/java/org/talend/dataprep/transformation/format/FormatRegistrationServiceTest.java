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

import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.format.export.ExportFormat;

/**
 * Unit test for the FormatRegistrationService.
 * 
 * @see FormatRegistrationService
 */
public class FormatRegistrationServiceTest extends BaseFormatTest {

    /** The service to test. */
    @Autowired
    private FormatRegistrationService service;

    @Test
    public void shouldListOnlyExternalFormats() {
        final Stream<ExportFormat> externalFormats = service.getExternalFormats();
        externalFormats.forEach(format -> Assert.assertFalse(StringUtils.equals(JsonFormat.JSON, format.getName())));
    }

    @Test
    public void shouldGetFormatByItsName() {
        final ExportFormat csvFormat = service.getByName(CSVFormat.CSV);
        Assert.assertTrue(csvFormat instanceof CSVFormat);
    }

    @Test
    public void shouldNotGetAnyFormat() {
        final ExportFormat noFormat = service.getByName("toto");
        Assert.assertNull(noFormat);
    }
}