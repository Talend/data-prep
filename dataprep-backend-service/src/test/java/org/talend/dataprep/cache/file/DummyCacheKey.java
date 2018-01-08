//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.cache.file;

import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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
        return this.getClass().getSimpleName() + '_' + name + '_' + Objects.hash(name, random);
    }

    @Override
    public Predicate<String> getMatcher() {
        final String regex = this.getClass().getSimpleName()
                + '_'
                + (name == null ? ".*" : name)
                + "_.*";
        final Pattern pattern = Pattern.compile(regex);
        return key -> pattern.matcher(key).matches();
    }
}
