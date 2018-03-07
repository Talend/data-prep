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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.dataset.event.DataSetMetadataBeforeUpdateEvent;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.TransformationCacheKey;

@Component
/**
 * TODO: A SUPPRIMER ?
 */
public class DataSetMetadataBeforeUpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetMetadataBeforeUpdateListener.class);

    @Autowired
    private ContentCache contentCache;

    @Autowired
    private CacheKeyGenerator generator;

    @Autowired
    protected ApplicationEventPublisher publisher;

    @EventListener
    public void onMetadataBeforeUpdateEvent(DataSetMetadataBeforeUpdateEvent event) {
        System.out.println("|||||||||||||||||||||||||||||||||||||||||||-");
        System.out.println("DataSetMetadataBeforeUpdateListener.onMetadataBeforeUpdateEvent");
        System.out.println("event = [" + event + "]");
        System.out.println("|||||||||||||||||||||||||||||||||||||||||||-");

        final DataSetMetadata dataSetMetadata = event.getSource();

        // Evict transformation cache that uses updated dataset
        final TransformationCacheKey transformationCacheKey = generator.generateContentKey(dataSetMetadata.getId(), //
                null, //
                null, //
                null, //
                null, //
                null);

        LOGGER.debug("Evicting sample cache entry for #{}", dataSetMetadata.getId());
//        publisher.publishEvent(new CleanCacheEvent(sampleKey));
        LOGGER.debug("Evicting sample cache entry for #{} done.", dataSetMetadata.getId());


        contentCache.evictMatch(transformationCacheKey);
    }

}
