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

package org.talend.dataprep.transformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for all unit/integration tests for transformation.
 */
public abstract class TransformationBaseTest extends ServiceBaseTest {

    @Autowired
    protected DataSetMetadataBuilder metadataBuilder;

    @Autowired
    protected ObjectMapper mapper;

}
