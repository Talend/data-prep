package org.talend.dataprep.transformation.format;

import java.util.List;

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
        final List<ExportFormat> externalFormats = service.getExternalFormats();
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