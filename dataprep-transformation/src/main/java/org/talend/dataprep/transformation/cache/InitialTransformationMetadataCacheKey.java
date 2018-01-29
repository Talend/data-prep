//  ============================================================================
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

package org.talend.dataprep.transformation.cache;

import org.talend.dataprep.api.export.ExportParameters;

public class InitialTransformationMetadataCacheKey extends TransformationMetadataCacheKey{

    private static final String PREFIX = "initial-transformation-metadata";

    public InitialTransformationMetadataCacheKey(String preparationId, String stepId, ExportParameters.SourceType sourceType, String userId) {
        super(preparationId, stepId, sourceType, userId);
    }

    @Override
    public String getKey() {
        return PREFIX + "_" + getPreparationId() + "_" + getStepId() + "_" + getSourceType() + "_" + getUserId();
    }
}
