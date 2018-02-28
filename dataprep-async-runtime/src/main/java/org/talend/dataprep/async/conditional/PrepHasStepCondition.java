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

package org.talend.dataprep.async.conditional;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.export.ExportParametersUtil;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;

/**
 * Return True if a preparation has more than the initial step
 */
@Component
public class PrepHasStepCondition implements ConditionalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrepHasStepCondition.class);

    @Autowired
    private ContentCache contentCache;

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

        String preparationId = (String) args[0];
        String headId = (String) args[1];

        Preparation prep = exportParametersUtil.getPreparation(preparationId, headId);

        return prep.getSteps().size() > 1;
    }
}
