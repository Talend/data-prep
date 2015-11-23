package org.talend.dataprep.dataset;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.service.analysis.SynchronousDataSetAnalyzer;

/**
 * This test ensures the data set service behaves as stated in <a
 * href="https://jira.talendforge.org/browse/TDP-157">https://jira.talendforge.org/browse/TDP-157</a>.
 */

public class DataSetImportTest extends DataSetBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetImportTest.class);

    private String dataSetId;

    @BeforeClass
    public static void enter() {
        // Set pause in analysis
        System.setProperty("DataSetImportTest.PausedAnalyzer", "1"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @AfterClass
    public static void leave() {
        // Set pause in analysis
        System.setProperty("DataSetImportTest.PausedAnalyzer", "0"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Test 'importing' status: the data set should remain in 'importing' state as long as create operation isn't
     * completed.
     */
    @Test
    public void testImportStatus() throws Exception {

        // Create a data set (asynchronously)
        Runnable creation = () -> {
            try {
                dataSetId = given().body(IOUtils.toString(DataSetImportTest.class.getResourceAsStream("tagada.csv")))
                        .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
                LOGGER.debug("testImportStatus dataset created #{}", dataSetId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        Thread creationThread = new Thread(creation);
        creationThread.start();
        // Wait for creation of data set object
        while (!dataSetMetadataRepository.list().iterator().hasNext()) {
            TimeUnit.MILLISECONDS.sleep(20);
        }
        // Data set should show as importing
        final Iterable<DataSetMetadata> list = dataSetMetadataRepository.list();
        final Iterator<DataSetMetadata> iterator = list.iterator();
        assertThat(iterator.hasNext(), is(true));
        final DataSetMetadata next = iterator.next();
        assertThat(next.getLifecycle().importing(), is(true));
        // Asserts when import is done
        // Wait until creation is done (i.e. end of thread since creation is a blocking operation).
        creationThread.join();
        assertThat(dataSetId, notNullValue());
        final DataSetMetadata metadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(metadata.getLifecycle().importing(), is(false));
        assertThat(metadata.getLifecycle().schemaAnalyzed(), is(true));
        // TDP-283: Quality analysis should be synchronous
        assertThat(metadata.getLifecycle().qualityAnalyzed(), is(true));
    }

    /**
     * Test 'importing' status with list operation: data set in 'importing' mode should not appear in results of the
     * list operation.
     */
    @Test
    public void testListImported() throws Exception {
        assertThat(dataSetMetadataRepository.size(), is(0));
        // Create a data set (asynchronously)
        Runnable creation = () -> {
            try {
                dataSetId = given().body(IOUtils.toString(DataSetImportTest.class.getResourceAsStream("tagada.csv")))
                        .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        Thread creationThread = new Thread(creation);
        creationThread.start();
        // Wait for creation of data set object
        while (dataSetMetadataRepository.size() == 0) {
            TimeUnit.MILLISECONDS.sleep(20);
        }
        // Find data set being imported...
        final Iterable<DataSetMetadata> list = dataSetMetadataRepository.list();
        final Iterator<DataSetMetadata> iterator = list.iterator();
        assertThat(iterator.hasNext(), is(true));
        final DataSetMetadata next = iterator.next();
        assertThat(next.getLifecycle().importing(), is(true));
        // ... list operation should *not* return data set being imported...
        when().get("/datasets").then().statusCode(HttpStatus.OK.value()).body(equalTo("[]"));
        // Assert the new data set is returned when creation completes.
        // Wait until creation is done (i.e. end of thread since creation is a blocking operation).
        creationThread.join();
        assertThat(dataSetId, notNullValue());
        final DataSetMetadata metadata = dataSetMetadataRepository.get(dataSetId);
        assertThat(dataSetMetadataRepository.size(), is(1));
        String expected = "[{\"id\":\"" + metadata.getId() + "\"}]";
        when().get("/datasets").then().statusCode(HttpStatus.OK.value())
                .body(sameJSONAs(expected).allowingAnyArrayOrdering().allowingExtraUnexpectedFields());
    }

    /**
     * Test 'importing' status with get: user is not allowed to get data set content when it's still being imported. In
     * real life situation, this kind of event is rather unlikely since the UUID of the data set is only returned once
     * the creation completes (no access before this).
     */
    @Test
    public void testCannotOpenDataSetBeingImported() throws Exception {

        LOGGER.info("testCannotOpenDataSetBeingImported started");

        assertThat(dataSetMetadataRepository.size(), is(0));
        LOGGER.debug("dataSetMetadata repository is empty");

        // Create a data set (asynchronously)
        Runnable creation = () -> {
            try {
                dataSetId = given().body(IOUtils.toString(DataSetImportTest.class.getResourceAsStream("tagada.csv")))
                        .queryParam("Content-Type", "text/csv").when().post("/datasets").asString();
                LOGGER.debug("testCannotOpenDataSetBeingImported dataset created #{}", dataSetId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Thread creationThread = new Thread(creation);
        creationThread.start();
        // Wait for creation of data set object
        while (dataSetMetadataRepository.size() == 0) {
            TimeUnit.MILLISECONDS.sleep(20);
        }
        // Find data set being imported...
        final Iterator<DataSetMetadata> iterator = dataSetMetadataRepository.list().iterator();
        assertThat(iterator.hasNext(), is(true));
        final DataSetMetadata next = iterator.next();
        LOGGER.info("found {}", next);
        assertThat(next.getLifecycle().importing(), is(true));
        // ... get operation should *not* return data set being imported but report an error ...
        int statusCode = when().get("/datasets/{id}/content", next.getId()).getStatusCode();
        assertThat(statusCode, is(400));
        // Assert the new data set is returned when creation completes.
        // Wait until creation is done (i.e. end of thread since creation is a blocking operation).
        creationThread.join();
        assertThat(dataSetId, notNullValue());
        final DataSetMetadata metadata = dataSetMetadataRepository.get(dataSetId);
        statusCode = when().get("/datasets/{id}/content", metadata.getId()).getStatusCode();
        assertThat(statusCode, is(200));
    }

    /**
     * A special (for tests) implementation of {@link SynchronousDataSetAnalyzer} that allows test code to intentionally
     * slow down import process for test purposes.
     */
    @Component
    public static class PausedAnalyzer implements SynchronousDataSetAnalyzer {

        private static final Logger LOGGER = LoggerFactory.getLogger(PausedAnalyzer.class);

        @Override
        public int order() {
            return Integer.MAX_VALUE - 1;
        }

        @Override
        public void analyze(String dataSetId) {
            if (StringUtils.isEmpty(dataSetId)) {
                throw new IllegalArgumentException("Data set id cannot be null or empty.");
            }
            try {
                String timeToPause = System.getProperty("DataSetImportTest.PausedAnalyzer");
                if (!StringUtils.isEmpty(timeToPause)) {
                    LOGGER.info("Pausing import (for {} second(s))...", timeToPause);
                    TimeUnit.SECONDS.sleep(Integer.parseInt(timeToPause));
                    LOGGER.info("Pause done.");
                } else {
                    LOGGER.info("No pause.");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to pause import.", e);
            }
        }
    }
}
