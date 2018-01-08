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

package org.talend.dataprep.api.service;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.service.test.APIClientTest;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.dataset.store.content.DataSetContentStore;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.aggregation.api.AggregationParameters;
import org.talend.dataprep.url.UrlRuntimeUpdater;

/**
 * Base test for all API service unit.
 */
public abstract class ApiServiceTestBase extends ServiceBaseTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ApiServiceTestBase.class);

    @Autowired
    protected DataSetMetadataRepository dataSetMetadataRepository;

    @Autowired
    @Qualifier("ContentStore#local")
    protected DataSetContentStore contentStore;

    @Autowired
    protected PreparationRepository preparationRepository;

    @Autowired
    protected ContentCache cache;

    @Autowired
    protected FolderRepository folderRepository;

    @Autowired
    protected APIClientTest testClient;

    protected Folder home;

    @Autowired
    private UrlRuntimeUpdater[] urlUpdaters;

    @Before
    public void setUp() {
        super.setUp();
        for(UrlRuntimeUpdater urlUpdater : urlUpdaters) {
            urlUpdater.setUp();
        }
        home = folderRepository.getHome();
    }

    @After
    public void tearDown() {
        dataSetMetadataRepository.clear();
        contentStore.clear();
        preparationRepository.clear();
        cache.clear();
        folderRepository.clear();
    }

    protected AggregationParameters getAggregationParameters(String input) throws IOException {
        InputStream parametersInput = this.getClass().getResourceAsStream(input);
        return mapper.readValue(parametersInput, AggregationParameters.class);
    }
}
