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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Return TRUE if preparation are not in cache and export deals with preparation
 */
@Component
public class GetPrepContentAsyncCondition implements ConditionalTest {

    @Autowired
    private PreparationCacheCondition cacheCondition;

    @Autowired
    private PreparationExportCondition exportCondition;

    @Override
    public boolean apply(Object... args) {

        return !cacheCondition.apply(args) && exportCondition.apply(args);
    }
}
