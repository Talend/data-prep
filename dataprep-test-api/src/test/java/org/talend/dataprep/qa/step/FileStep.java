// ============================================================================
//
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

package org.talend.dataprep.qa.step;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.config.DataPrepStep;

import cucumber.api.java.en.Then;
import org.talend.dataprep.qa.util.ExcelComparator;

/**
 * Store all steps related to files and temporary files.
 */
public class FileStep extends DataPrepStep {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FileStep.class);

    @Then("^I check that \"(.*)\" temporary file equals \"(.*)\" file$")
    public void thenICheckThatTheFileIsTheExpectedOne(String temporaryFilename, String expectedCSVFilename) throws IOException {
        LOG.debug("I check that {} temporary file equals {} file", temporaryFilename, expectedCSVFilename);

        File tempFile = context.getTempFile(temporaryFilename);
        try (InputStream tempFileStream = Files.newInputStream(tempFile.toPath());
                InputStream expectedFileStream = DataPrepStep.class.getResourceAsStream(expectedCSVFilename)) {
            if (!IOUtils.contentEquals(tempFileStream, expectedFileStream)) {
                fail("Temporary file " + temporaryFilename + " isn't the same as the expected file " + expectedCSVFilename);
            }
        }
    }

    @Then("^I check that XLSX \"(.*)\" temporary file equals \"(.*)\" file$")
    public void thenICheckThatTheXLSXFileIsTheExpectedOne(String temporaryFilename, String expectedCSVFilename) throws IOException {
        LOG.debug("I check that {} temporary file equals {} file", temporaryFilename, expectedCSVFilename);

        File tempFile = context.getTempFile(temporaryFilename);

        try (InputStream tempFileStream = Files.newInputStream(tempFile.toPath());
             InputStream expectedFileStream = DataPrepStep.class.getResourceAsStream(expectedCSVFilename)) {

            XSSFWorkbook tempXLSX = new XSSFWorkbook(tempFileStream);
            XSSFWorkbook expectedXLSX = new XSSFWorkbook(expectedFileStream);

            if (!ExcelComparator.compareTwoFile(tempXLSX, expectedXLSX)) {
                fail("Temporary file " + temporaryFilename + " isn't the same as the expected file " + expectedCSVFilename);
            }
        }
    }
}
