package org.talend.dataprep.cache.file;

import java.util.Objects;
import java.util.Random;

import org.talend.dataprep.cache.ContentCacheKey;

/**
 * Dummy implementation of ContentCacheKey used solely for unit tests.
 */
public class DummyCacheKey implements ContentCacheKey {

    private final static Random RANDOMIZE = new Random();

    /** A name. */
    private String name;

    /** Random long. */
    private long random;

    /**
     * Default constructor.
     * 
     * @param name the content name.
     */
    public DummyCacheKey(String name) {
        this.name = name;
        this.random = RANDOMIZE.nextLong();
    }

    /**
     * @return the key for this cache content as a string.
     */
    @Override
    public String getKey() {
        return this.getClass().getSimpleName() + '-' + Objects.hash(name, random);
    }
}
