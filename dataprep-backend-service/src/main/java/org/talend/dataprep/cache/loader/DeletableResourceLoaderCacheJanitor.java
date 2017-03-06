// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.cache.loader;

import static java.util.Arrays.stream;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.talend.daikon.content.DeletablePathResolver;
import org.talend.daikon.content.DeletableResource;
import org.talend.dataprep.cache.CacheJanitor;

@Component
@ConditionalOnBean(DeletablePathResolver.class)
@EnableScheduling
public class DeletableResourceLoaderCacheJanitor implements CacheJanitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeletableResourceLoaderCacheJanitor.class);

    @Autowired
    private DeletablePathResolver deletablePathResolver;

    @Override
    @Scheduled(fixedDelay = 60000)
    public void janitor() {
        final long start = System.currentTimeMillis();
        final AtomicLong deletedCount = new AtomicLong();
        final AtomicLong totalCount = new AtomicLong();
        LOGGER.debug("Janitor process started @ {}.", start);

        try {
            final DeletableResource[] resources = deletablePathResolver.getResources("/cache/*");
            final Predicate<DeletableResource> deleteOld = (resource) -> {
                final String fileName = resource.getFilename();
                final String suffix = StringUtils.substringAfterLast(fileName, ".");

                // Ignore "." files (hidden files like MacOS).
                if (resource.getFilename().startsWith(".")) {
                    return false;
                }
                // Ignore NFS files (may happen in local mode when NFS is used).
                if (suffix.startsWith("nfs")) {
                    return false;
                }
                if (StringUtils.isEmpty(suffix)) {
                    return false;
                }

                try {
                    final long time = Long.parseLong(suffix);
                    if (time < start) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.debug("Ignore file '{}'", resource);
                }
                totalCount.incrementAndGet();
                return false;
            };

            // Perform deletes for old resources
            stream(resources).filter(deleteOld).forEach(r -> {
                try {
                    deletedCount.incrementAndGet();
                    r.delete();
                } catch (IOException e) {
                    LOGGER.error("Unable to delete resource {}", r, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Unable to clean up resources", e);
        }

        LOGGER.debug("Janitor process ended @ {} ({}/{} files successfully deleted).", System.currentTimeMillis(), deletedCount,
                totalCount);
    }

}
