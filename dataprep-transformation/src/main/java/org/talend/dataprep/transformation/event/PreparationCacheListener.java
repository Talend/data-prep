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

package org.talend.dataprep.transformation.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.dataset.event.DataSetMetadataBeforeUpdateEvent;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;

@Component
public class PreparationCacheListener {

    @Autowired
    private ContentCache contentCache;

    @Autowired
    private CacheKeyGenerator generator;

    @EventListener
    public void onMetadataBeforeUpdateEvent(DataSetMetadataBeforeUpdateEvent event) {
        final DataSetMetadata dataSetMetadata = event.getSource();

        // Evict transformation cache that uses updated dataset
        final TransformationCacheKey transformationCacheKey = generator.generateContentKey(dataSetMetadata.getId(), //
                null, //
                null, //
                null, //
                null, //
                null);
        contentCache.evictMatch(transformationCacheKey);
    }

}
