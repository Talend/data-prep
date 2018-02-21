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

package org.talend.dataprep.async.conditional;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.export.ExportParametersUtil;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.cache.TransformationMetadataCacheKey;

import java.io.IOException;

import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;

/**
 * Return TRUE if preparation metadata is in cache
 */
@Component
public class PrepMetadataCacheCondition implements ConditionalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrepMetadataCacheCondition.class);

    @Autowired
    private CacheCondition cacheCondition;

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    @Autowired
    private ExportParametersUtil exportParametersUtil;

    @Override
    public boolean apply(Object... args) {

        // check pre-condition
        Validate.notNull(args);
        Validate.isTrue(args.length == 2);
        Validate.isInstanceOf(String.class, args[0]);
        Validate.isInstanceOf(String.class, args[1]);

        try {
            String preparationId = (String) args[0];
            String headId = (String) args[1];

            ExportParameters exportParameters = new ExportParameters();
            exportParameters.setPreparationId(preparationId);
            exportParameters.setStepId(headId);
            exportParameters = exportParametersUtil.populateFromPreparationExportParameter(exportParameters);

            final TransformationMetadataCacheKey cacheKey = cacheKeyGenerator.generateMetadataKey(exportParameters.getPreparationId(), exportParameters.getStepId(), HEAD);

            return cacheCondition.apply(cacheKey);

        } catch (IOException e) {
            LOGGER.error("Cannot get all information from export parameters", e);
        }

        return false;
    }
}
