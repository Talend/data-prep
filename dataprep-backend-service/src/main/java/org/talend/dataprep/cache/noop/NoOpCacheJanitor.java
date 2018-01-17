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

package org.talend.dataprep.cache.noop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.talend.daikon.content.ContentServiceEnabled;
import org.talend.dataprep.cache.CacheJanitor;

@Component
@ConditionalOnMissingBean(ContentServiceEnabled.class)
public class NoOpCacheJanitor implements CacheJanitor {

    @Override
    public void janitor() {
        // Nothing to do.
    }
}
