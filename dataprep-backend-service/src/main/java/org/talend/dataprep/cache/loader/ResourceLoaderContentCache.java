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

package org.talend.dataprep.cache.loader;

import static java.lang.Long.parseLong;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.function.BinaryOperator.maxBy;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.talend.daikon.content.ContentServiceEnabled;
import org.talend.daikon.content.DeletableResource;
import org.talend.daikon.content.ResourceResolver;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;

@Component
@ConditionalOnBean(ContentServiceEnabled.class)
public class ResourceLoaderContentCache implements ContentCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoaderContentCache.class);

    private static final String CACHE_PREFIX = "/cache/";

    @Autowired
    private ResourceResolver resolver;

    public ResourceLoaderContentCache() {
        LOGGER.info("Using content cache: {}", this.getClass().getName());
    }

    private DeletableResource getOrCreateResource(ContentCacheKey key, TimeToLive ttl) {
        return resolver.getResource(getLocation(key, ttl));
    }

    private String getLocation(ContentCacheKey key, TimeToLive ttl) {
        if (ttl.getTime() > 0) {
            return CACHE_PREFIX + key.getKey() + "." + (System.currentTimeMillis() + ttl.getTime());
        } else {
            return CACHE_PREFIX + key.getKey();
        }
    }

    private DeletableResource getResource(ContentCacheKey key) {
        try {
            final DeletableResource[] patternMatches = resolver.getResources(CACHE_PREFIX + key.getKey() + "*");
            final DeletableResource[] directMatches = resolver.getResources(CACHE_PREFIX + key.getKey());
            final DeletableResource[] resources = new DeletableResource[patternMatches.length + directMatches.length];
            System.arraycopy(patternMatches, 0, resources, 0, patternMatches.length);
            System.arraycopy(directMatches, 0, resources, patternMatches.length, directMatches.length);

            if (resources.length <= 0) {
                return null;
            } else { // resources.length > 0
                final Optional<DeletableResource> reduce = stream(resources).reduce(maxBy((r1, r2) -> {
                    final String suffix1 = substringAfterLast(r1.getFilename(), ".");
                    final String suffix2 = substringAfterLast(r2.getFilename(), ".");
                    if (StringUtils.isEmpty(suffix1) || StringUtils.isEmpty(suffix2)) {
                        return 0;
                    }

                    final long i1 = parseLong(suffix1);
                    final long i2 = parseLong(suffix2);
                    return Long.compare(i1, i2);
                }));
                return reduce.filter(r -> {
                    if (!r.exists()) {
                        return false;
                    }
                    final String suffix = StringUtils.substringAfterLast(r.getFilename(), ".");
                    if (NumberUtils.isCreatable(suffix)) {
                        final long time = parseLong(suffix);
                        return time > System.currentTimeMillis();
                    } else {
                        return true;
                    }
                }).orElse(null);
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Timed
    @Override
    public boolean has(ContentCacheKey key) {
        final boolean present = ofNullable(getResource(key)).isPresent();
        LOGGER.debug("Has '{}': {}", key.getKey(), present);
        return present;
    }

    @Timed
    @VolumeMetered
    @Override
    public InputStream get(ContentCacheKey key) {
        LOGGER.debug("Get '{}'", key.getKey());
        return ofNullable(getResource(key)).map(r -> {
            try {
                return r.getInputStream();
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        }).orElse(null);
    }

    @Timed
    @VolumeMetered
    @Override
    public OutputStream put(ContentCacheKey key, TimeToLive timeToLive) {
        LOGGER.debug("Put '{}' (TTL: {})", key.getKey(), timeToLive);
        try {
            return getOrCreateResource(key, timeToLive).getOutputStream();
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Timed
    @Override
    public void evict(ContentCacheKey key) {
        LOGGER.debug("Evict '{}'", key.getKey());
        ofNullable(getResource(key)).ifPresent(r -> {
            try {
                r.delete();
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        });
    }

    @Timed
    @Override
    public void evictMatch(ContentCacheKey key) {
        LOGGER.debug("Evict match '{}'", key.getKey());
        try {
            final DeletableResource[] resources = resolver.getResources(CACHE_PREFIX + key.getPrefix() + "**");
            final Predicate<String> matcher = key.getMatcher();
            stream(resources).filter(r -> {
                String fileName = r.getFilename();
                if (fileName.contains(CACHE_PREFIX)) {
                    fileName = fileName.substring(fileName.indexOf(CACHE_PREFIX) + CACHE_PREFIX.length());
                }
                return matcher.test(fileName);
            }).forEach(r -> {
                try {
                    LOGGER.debug("Delete file '{}'.", r.getFilename());
                    r.delete();
                } catch (IOException e) {
                    throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                }
            });
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Timed
    @Override
    public void move(ContentCacheKey from, ContentCacheKey to, TimeToLive toTimeToLive) {
        LOGGER.debug("Move '{}' -> '{}' (TTL: {})", from.getKey(), to.getKey(), toTimeToLive);
        final DeletableResource resource = getResource(from);
        if (resource != null) {
            final String destination = getLocation(to, toTimeToLive);
            if (!resource.exists()) {
                LOGGER.debug("Source file no longer exists.");
                if (resolver.getResource(destination).exists()) {
                    LOGGER.debug("No need to move file (destination already exists).");
                    return;
                } else {
                    LOGGER.error("Source file '{}' no longer exists, neither does destination '{}'", from.getKey(),
                            destination);
                    throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION);
                }
            }
            try {
                resource.move(destination);
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        }
    }

    @Timed
    @Override
    public void clear() {
        LOGGER.debug("Clear all");
        try {
            resolver.clear(CACHE_PREFIX + "**");
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
