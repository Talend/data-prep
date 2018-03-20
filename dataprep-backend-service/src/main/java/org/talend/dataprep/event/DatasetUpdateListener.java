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

package org.talend.dataprep.event;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.dataset.event.DatasetUpdatedEvent;

@Component
public class DatasetUpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetUpdateListener.class);

    @Autowired
    protected ApplicationEventPublisher publisher;

    @EventListener
    public void onUpdate(DatasetUpdatedEvent event) {

        // when we update a dataset we need to clean cache
        final DataSetMetadata dataSetMetadata = event.getSource();
        final ContentCacheKey sampleKey = () -> "dataset-sample_" + dataSetMetadata.getId();
        LOGGER.debug("Evicting sample cache entry for #{}", dataSetMetadata.getId());
        publisher.publishEvent(new CleanCacheEvent(sampleKey));
        LOGGER.debug("Evicting sample cache entry for #{} done.", dataSetMetadata.getId());

        LOGGER.debug("Evicting transformation cache entry for dataset #{}", dataSetMetadata.getId());
        publisher.publishEvent(new CleanCacheEvent(new ContentCacheKey() {

            @Override
            public String getKey() {
                return dataSetMetadata.getId();
            }

            @Override
            public Predicate<String> getMatcher() {
                String regex = ".*_" + getKey() + "_.*";

                // Build regular expression matcher
                final Pattern pattern = Pattern.compile(regex);
                return str -> pattern.matcher(str).matches();
            }

        }, Boolean.TRUE));
        LOGGER.debug("Evicting transformation cache entry for dataset  #{} done.", dataSetMetadata.getId());
    }
}
