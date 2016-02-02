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

package org.talend.dataprep.dataset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.talend.dataprep.api.dataset.DataSetLifecycle;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.dataset.service.Destinations;
import org.talend.dataprep.dataset.store.content.DataSetContentStore;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.schema.FormatGuess;
import org.talend.dataprep.user.store.UserDataRepository;

import com.jayway.restassured.RestAssured;

/**
 * Base class for DataSet integration tests.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public abstract class DataSetBaseTest {

    @Value("${local.server.port}")
    public int port;

    @Autowired
    protected DataSetMetadataRepository dataSetMetadataRepository;

    @Autowired
    protected UserDataRepository userDataRepository;

    @Autowired
    protected JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("ContentStore#local")
    protected DataSetContentStore contentStore;

    @Autowired
    protected FormatGuess.Factory factory;

    @Autowired
    protected DataSetMetadataBuilder metadataBuilder;

    /** This class" logger. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected void assertQueueMessages(String dataSetId) throws Exception {
        // Wait for queue messages
        waitForQueue(Destinations.STATISTICS_ANALYSIS, dataSetId);
        // Asserts on metadata status
        DataSetMetadata metadata = dataSetMetadataRepository.get(dataSetId);
        assertNotNull(metadata);
        DataSetLifecycle lifecycle = metadata.getLifecycle();
        assertThat(lifecycle.contentIndexed(), is(true));
        assertThat(lifecycle.schemaAnalyzed(), is(true));
        assertThat(lifecycle.qualityAnalyzed(), is(true));
    }

    protected void waitForQueue(String queueName, String dataSetId) {
        // Wait for potential update still in progress
        final DistributedLock lock = dataSetMetadataRepository.createDatasetMetadataLock(dataSetId);
        lock.lock();
        lock.unlock();
        // Ensure queues are empty
        try {
            boolean isEmpty = false;
            while (!isEmpty) {
                isEmpty = jmsTemplate.browse(queueName, (session, browser) -> !browser.getEnumeration().hasMoreElements());
                if (!isEmpty) {
                    Thread.sleep(200);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() throws IOException {
        RestAssured.port = port;
        dataSetMetadataRepository.clear();
        contentStore.clear();
        userDataRepository.clear();
    }

    @org.junit.After
    public void tearDown() throws IOException {
        dataSetMetadataRepository.clear();
        contentStore.clear();
    }
}
