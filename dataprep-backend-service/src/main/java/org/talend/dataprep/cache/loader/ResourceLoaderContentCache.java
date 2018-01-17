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
import static org.apache.commons.lang.StringUtils.substringAfterLast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
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

    @Autowired
    private ResourceResolver resolver;

    private DeletableResource getOrCreateResource(ContentCacheKey key, TimeToLive ttl) {
        return resolver.getResource(getLocation(key, ttl));
    }

    private String getLocation(ContentCacheKey key, TimeToLive ttl) {
        if (ttl.getTime() > 0) {
            return "/cache/" + key.getKey() + "." + (System.currentTimeMillis() + ttl.getTime());
        } else {
            return "/cache/" + key.getKey();
        }
    }

    private DeletableResource getResource(ContentCacheKey key) {
        try {
            final DeletableResource[] resources = resolver.getResources( "/cache/" + key.getKey() + "*");
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
                    if (i1 > i2) {
                        return 1;
                    } else if (i1 < i2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }));
                return reduce.filter(r -> {
                    final String suffix = StringUtils.substringAfterLast(r.getFilename(), ".");
                    try {
                        final long time = parseLong(suffix);
                        return time > System.currentTimeMillis();
                    } catch (NumberFormatException e) {
                        // Ignored
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
        return ofNullable(getResource(key)).isPresent();
    }

    @VolumeMetered
    @Override
    public InputStream get(ContentCacheKey key) {
        return ofNullable(getResource(key)).map(r -> {
            try {
                return r.getInputStream();
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        }).orElse(null);
    }

    @VolumeMetered
    @Override
    public OutputStream put(ContentCacheKey key, TimeToLive timeToLive) {
        try {
            return getOrCreateResource(key, timeToLive).getOutputStream();
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Timed
    @Override
    public void evict(ContentCacheKey key) {
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
        try {
            final DeletableResource[] resources = resolver.getResources("/cache/" + key.getPrefix() + "**");
            final Predicate<String> matcher = key.getMatcher();
            stream(resources).filter(r -> matcher.test(r.getFilename())).forEach(r -> {
                try {
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
        final DeletableResource resource = getResource(from);
        if (resource != null) {
            try {
                resource.move(getLocation(to, toTimeToLive));
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        }
    }

    @Timed
    @Override
    public void clear() {
        try {
            resolver.clear("/cache/**");
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
