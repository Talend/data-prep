package org.talend.dataprep.transformation.api.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;

import java.io.IOException;
import java.io.OutputStream;

public class ConfiguredCacheWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfiguredCacheWriter.class);

    private final ContentCache contentCache;

    private ContentCache.TimeToLive ttl;

    public ConfiguredCacheWriter(final ContentCache contentCache, final ContentCache.TimeToLive ttl) {
        this.contentCache = contentCache;
        this.ttl = ttl;
    }

    public void write(final ContentCacheKey key, final Object object) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectWriter objectWriter = mapper.writerFor(object.getClass());
        try (final OutputStream output = contentCache.put(key, ttl)) {
            objectWriter.writeValue(output, object);
            LOGGER.debug("New metadata cache entry -> {}.", key.getKey());
        }
    }
}
