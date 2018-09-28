package org.talend.dataprep.cache;

import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;

/**
 * An implementation of {@link ContentCache} that GZIP content for {@link #get(ContentCacheKey) gets} and
 * {@link #put(ContentCacheKey, TimeToLive) puts}.
 */
public class ZippedContentCache implements ContentCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZippedContentCache.class);

    private final ContentCache delegate;

    public ZippedContentCache(ContentCache delegate) {
        this.delegate = delegate;
    }

    @Override
    @Timed
    public boolean has(ContentCacheKey key) {
        return delegate.has(key);
    }

    @Override
    @VolumeMetered
    public InputStream get(ContentCacheKey key) {
        return ofNullable(delegate.get(key)) //
                .map(entry -> {
                    try {
                        return new GZIPInputStream(entry);
                    } catch (ZipException e) {
                        try {
                            entry.close();
                        } catch (IOException closeException) {
                            LOGGER.debug("Unable to close stream", e);
                        }
                        return delegate.get(key);
                    } catch (IOException e) {
                        throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                    }
                }) //
                .orElse(null);
    }

    @Override
    @VolumeMetered
    public OutputStream put(ContentCacheKey key, TimeToLive timeToLive) {
        return ofNullable(delegate.put(key, timeToLive)) //
                .map(entry -> {
                    try {
                        return new GZIPOutputStream(entry);
                    } catch (IOException e) {
                        throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                    }
                }) //
                .orElse(null);
    }

    @Override
    @Timed
    public void evict(ContentCacheKey key) {
        delegate.evict(key);
    }

    @Override
    @Timed
    public void evictMatch(ContentCacheKey key) {
        delegate.evictMatch(key);
    }

    @Override
    @Timed
    public void move(ContentCacheKey from, ContentCacheKey to, TimeToLive toTimeToLive) {
        delegate.move(from, to, toTimeToLive);
    }

    @Override
    @Timed
    public void clear() {
        delegate.clear();
    }
}
