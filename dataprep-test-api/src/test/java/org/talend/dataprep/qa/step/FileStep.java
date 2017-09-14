package org.talend.dataprep.qa.step;

import cucumber.api.java8.En;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Store all steps related to files and temporary files.
 */
public class FileStep extends DataPrepStep implements En {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FileStep.class);

    /**
     * Default constructor
     */
    public FileStep() {

        Then("^I check that \"(.*)\" temporary file equals \"(.*)\" file$", (String temporaryFilename, String expectedCSVFilename) -> {
            LOG.debug("I check that {} temporary file equals {} file", temporaryFilename, expectedCSVFilename);
            try {
                File tempFile = context.getTempFile(temporaryFilename);
                InputStream tempFileStream = Files.newInputStream(tempFile.toPath());
                InputStream expectedFileStream = DataPrepStep.class.getResourceAsStream(expectedCSVFilename);
                if (!IOUtils.contentEquals(tempFileStream, expectedFileStream)) {
                    Assert.fail("Temporary file " + temporaryFilename + " isn't the same as the expected file " + expectedCSVFilename);
                }
            } catch (IOException ioException) {
                LOG.error("Unable to compare temporary file {} with file {}", temporaryFilename, expectedCSVFilename, ioException);
            }
        });
    }
}
