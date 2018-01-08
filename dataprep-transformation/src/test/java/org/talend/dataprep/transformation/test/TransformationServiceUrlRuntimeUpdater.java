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

package org.talend.dataprep.transformation.test;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.talend.dataprep.transformation.service.BaseTransformationService;
import org.talend.dataprep.url.UrlRuntimeUpdater;

import com.jayway.restassured.RestAssured;

/**
 * Update the TransformationService url at runtime. This is needed for the tests.
 */
@Component
@Lazy
public class TransformationServiceUrlRuntimeUpdater extends UrlRuntimeUpdater {

    /** Get ALL transformation service implementations (also from the EE repository). */
    @Autowired
    private List<BaseTransformationService> transformationServices;

    /**
     * This method should be called before each test.
     */
    @Override
    public void setUp() {

        RestAssured.port = port;

        // set the service url @runtime
        for (BaseTransformationService service : transformationServices) {
            setField(service, "datasetServiceUrl", "http://localhost:" + port);
            setField(service, "preparationServiceUrl", "http://localhost:" + port);
        }
    }
}
