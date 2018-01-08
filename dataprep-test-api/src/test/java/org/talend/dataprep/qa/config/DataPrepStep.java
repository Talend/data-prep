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

package org.talend.dataprep.qa.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.talend.dataprep.helper.OSDataPrepAPIHelper;
import org.talend.dataprep.qa.SpringContextConfiguration;
import org.talend.dataprep.qa.dto.PreparationDetails;
import org.talend.dataprep.qa.util.OSIntegrationTestUtil;
import org.talend.dataprep.qa.util.folder.FolderUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;

/**
 * Base class for all DataPrep step classes.
 */
@ContextConfiguration(classes = SpringContextConfiguration.class, loader = AnnotationConfigContextLoader.class)
public abstract class DataPrepStep {

    /** {@link cucumber.api.DataTable} key for origin folder. */
    public static final String ORIGIN = "origin";

    /** {@link cucumber.api.DataTable} key for preparationName value. */
    public static final String PREPARATION_NAME = "preparationName";

    @Autowired
    protected FeatureContext context;

    @Autowired
    protected OSDataPrepAPIHelper api;

    @Autowired
    protected OSIntegrationTestUtil util;

    @Autowired
    protected FolderUtil folderUtil;

    protected ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Retrieve the details of a preparation from its id.
     *
     * @param preparationId the preparation id.
     * @return the preparation details.
     * @throws IOException
     */
    protected PreparationDetails getPreparationDetails(String preparationId) throws IOException {
        PreparationDetails preparationDetails = null;
        Response response = api.getPreparationDetails(preparationId);
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        preparationDetails = objectMapper.readValue(content, PreparationDetails.class);
        return preparationDetails;
    }

}
