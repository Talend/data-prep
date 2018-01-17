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

package org.talend.dataprep.cache.file;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.talend.dataprep.cache.ContentCache.TimeToLive.DEFAULT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.cache.CacheJanitor;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.cache.noop.NoOpCacheJanitor;

/**
 * Standard tests for a {@link ContentCache}
 *
 * @see LocalContentCacheTest
 */
public abstract class ContentCacheTests extends ServiceBaseTest {

    @Autowired
    ContentCache cache;

    @Autowired
    CacheJanitor janitor = new NoOpCacheJanitor();

    @After
    public void tearDown() throws Exception {
        cache.clear();
    }

    @Test
    public void testHasNot() throws Exception {
        // Cache is empty when test starts, has() must return false for content
        assertThat(cache.has(new DummyCacheKey("toto")), is(false));
    }

    @Test
    public void shouldHavePermanentEntry() throws Exception {
        // Put a content in cache...
        ContentCacheKey key = new DummyCacheKey("permanent");
        assertThat(cache.has(key), is(false));
        addCacheEntry(key, "content", ContentCache.TimeToLive.PERMANENT);
        // ... has() must return true
        assertThat(cache.has(key), is(true));
    }

    @Test
    public void shouldHaveWithMultipleVersions() throws Exception {
        // Put a content in cache...
        ContentCacheKey key = new DummyCacheKey("multipleVersions");
        assertThat(cache.has(key), is(false));
        addCacheEntry(key, "content", ContentCache.TimeToLive.DEFAULT);
        TimeUnit.MILLISECONDS.sleep(500);
        addCacheEntry(key, "content", ContentCache.TimeToLive.DEFAULT);
        // ... has() must return true
        assertThat(cache.has(key), is(true));
    }

    @Test
    public void shouldHaveWithMultipleVersionsAndDifferentTTL() throws Exception {
        // Put a content in cache...
        ContentCacheKey key = new DummyCacheKey("multipleVersions");
        assertThat(cache.has(key), is(false));
        addCacheEntry(key, "content", ContentCache.TimeToLive.DEFAULT);
        TimeUnit.MILLISECONDS.sleep(500);
        addCacheEntry(key, "content", ContentCache.TimeToLive.PERMANENT);
        // ... has() must return true
        assertThat(cache.has(key), is(true));
    }

    @Test
    public void shouldNotCleanPermanentEntryOnJanitor() throws Exception {
        // Put a content in cache...
        ContentCacheKey key = new DummyCacheKey("permanen");
        assertThat(cache.has(key), is(false));
        addCacheEntry(key, "content", ContentCache.TimeToLive.PERMANENT);

        // Call janitor
        janitor.janitor();

        // ... has() must return true
        assertThat(cache.has(key), is(true));
    }


    @Test
    public void testPutHas() throws Exception {
        // Put a content in cache...
        ContentCacheKey key = new DummyCacheKey("titi");
        assertThat(cache.has(key), is(false));
        addCacheEntry(key, "content", ContentCache.TimeToLive.DEFAULT);
        // ... has() must return true
        assertThat(cache.has(key), is(true));
    }

    @Test
    public void testGet() throws Exception {
        ContentCacheKey key = new DummyCacheKey("tata");
        String content = "yet another content...";
        // Put a content in cache...
        addCacheEntry(key, content, ContentCache.TimeToLive.DEFAULT);
        // ... get() should return this content back.
        final String actual = IOUtils.toString(cache.get(key), UTF_8);
        assertThat(actual, is(content));
    }

    @Test
    public void testEvictWithNoPut() throws Exception {
        ContentCacheKey key = new DummyCacheKey("tutu");
        assertThat(cache.has(key), is(false));
        // evict() a key that does not exist
        cache.evict(key);
        // ... has() must return false
        assertThat(cache.has(key), is(false));
    }

    @Test
    public void testEvict() throws Exception {
        ContentCacheKey key = new DummyCacheKey("tutu");
        // Put a content in cache...
        addCacheEntry(key, "content, yes again", ContentCache.TimeToLive.DEFAULT);
        assertThat(cache.has(key), is(true));
        // ... evict() it...
        cache.evict(key);
        // ... has() must immediately return false
        assertThat(cache.has(key), is(false));
    }

    @Test
    public void testEvictMatch() throws Exception {
        // given
        final ContentCacheKey key = new DummyCacheKey("youpala");
        final ContentCacheKey keyMatch = new DummyCacheKey("youpala");
        assertThat(cache.has(key), not(is(keyMatch.getKey()))); // not the same key because of random + hash

        // Put a content in cache...
        addCacheEntry(key, "content", DEFAULT);
        assertThat(cache.has(key), is(true));

        // when
        cache.evictMatch(key);

        // then
        assertThat(cache.has(key), is(false));
    }

    @Test
    public void testPermanentEntry() throws Exception {
        ContentCacheKey key = new DummyCacheKey("tutu");
        // Put a content in cache...
        addCacheEntry(key, "content, yes again", ContentCache.TimeToLive.PERMANENT);
        assertThat(cache.has(key), is(true));
        InputStream actualContentStream = cache.get(key);
        assertThat(IOUtils.toString(actualContentStream, UTF_8), is("content, yes again"));
        // Note : Closing stream so that cache can be evicted
        actualContentStream.close();
        // ... evict() it...
        cache.evict(key);
        // ... has() must immediately return false
        assertThat(cache.has(key), is(false));
        assertThat(cache.get(key), is((InputStream) null));
    }

    @Test
    public void testJanitorShouldNotCleanAnything() throws Exception {

        // given some valid cache entries
        List<ContentCacheKey> keys = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            keys.add(new DummyCacheKey("do not disturb-" + i + 1));
        }

        for (ContentCacheKey key : keys) {
            addCacheEntry(key, "content", ContentCache.TimeToLive.DEFAULT);
            assertThat(cache.has(key), is(true));
        }

        // when the janitor is called
        janitor.janitor();

        // then, none of the cache entries should be removed
        for (ContentCacheKey key : keys) {
            assertThat(cache.has(key), is(true));
        }
    }

    @Test
    public void testJanitor() throws Exception {

        // given some cache entries
        List<ContentCacheKey> keys = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            keys.add(new DummyCacheKey("janitor me " + i + 1));
        }
        for (ContentCacheKey key : keys) {
            addCacheEntry(key, "janitor content", ContentCache.TimeToLive.DEFAULT);
            assertThat(cache.has(key), is(true));
        }

        // when eviction is performed and the janitor is called
        for (ContentCacheKey key : keys) {
            cache.evict(key);
        }
        janitor.janitor();

        // then no file in the cache should be left
        for (int i = 0; i < 10; i++) {
            assertFalse(cache.has(new DummyCacheKey("janitor me " + i + 1)));
        }
    }

    @Test
    public void testJanitorEvictionPeriod() throws Exception {

        // given some cache entries
        List<ContentCacheKey> keys = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            keys.add(new DummyCacheKey("janitor me " + i + 1));
        }
        for (ContentCacheKey key : keys) {
            addCacheEntry(key, "janitor content", ContentCache.TimeToLive.IMMEDIATE);
            assertThat(cache.has(key), is(true));
        }

        // when eviction is performed and the janitor is called
        janitor.janitor();

        // then, none of the cache entries should be removed
        for (ContentCacheKey key : keys) {
            assertThat(cache.has(key), is(true));
        }

        Thread.sleep(ContentCache.TimeToLive.IMMEDIATE.getTime() + 500);

        // then, none of the cache entries should be removed
        for (ContentCacheKey key : keys) {
            assertThat(cache.has(key), is(false));
        }

        // when eviction is performed and the janitor is called
        janitor.janitor();
        for (ContentCacheKey key : keys) {
            assertFalse(cache.has(key));
        }
    }

    @Test
    public void testMove() throws Exception {
        // given
        final ContentCacheKey key1 = new DummyCacheKey("tata");
        final ContentCacheKey key2 = new DummyCacheKey("tata2");
        String content = "yet another content...";
        addCacheEntry(key1, content, ContentCache.TimeToLive.DEFAULT);
        assertTrue(cache.has(key1));
        assertFalse(cache.has(key2));

        // when
        cache.move(key1, key2, ContentCache.TimeToLive.DEFAULT);

        // then
        final String actual = IOUtils.toString(cache.get(key2), UTF_8);
        assertThat(actual, is(content));
        assertFalse(cache.has(key1));
        assertTrue(cache.has(key2));
    }

    /**
     * Add the cache entry.
     *
     * @param key where to put the cache entry.
     * @param content the cache entry content.
     * @param timeToLive the time to live for entry
     * @throws IOException if an error occurs.
     */
    private void addCacheEntry(ContentCacheKey key, String content, ContentCache.TimeToLive timeToLive) throws IOException {
        try (OutputStream entry = cache.put(key, timeToLive)) {
            entry.write(content.getBytes());
            entry.flush();
        }
    }
}
