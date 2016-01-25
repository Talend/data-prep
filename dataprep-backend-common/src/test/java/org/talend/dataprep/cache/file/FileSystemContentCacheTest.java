package org.talend.dataprep.cache.file;

import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;

public class FileSystemContentCacheTest {

    public static final String TEST_DIRECTORY = "target/cache/test";

    /** The content cache to test. */
    private FileSystemContentCache cache;

    @Before
    public void setUp() throws Exception {
        cache = new FileSystemContentCache(TEST_DIRECTORY);
    }

    @After
    public void tearDown() throws Exception {
        cache.clear();
    }

    @Test
    public void testHasNot() throws Exception {
        // Cache is empty when test starts, has() must return false for content
        Assert.assertThat(cache.has(new DummyCacheKey("toto")), is(false));
    }

    @Test
    public void testPutHas() throws Exception {
        // Put a content in cache...
        ContentCacheKey key = new DummyCacheKey("titi");
        Assert.assertThat(cache.has(key), is(false));
        addCacheEntry(key, "content");
        // ... has() must return true
        Assert.assertThat(cache.has(key), is(true));
    }


    @Test
    public void testGet() throws Exception {
        ContentCacheKey key = new DummyCacheKey("tata");
        String content = "yet another content...";
        // Put a content in cache...
        addCacheEntry(key, content);
        // ... get() should return this content back.
        final String actual = IOUtils.toString(cache.get(key));
        Assert.assertThat(actual, is(content));
    }

    @Test
    public void testEvict() throws Exception {
        ContentCacheKey key = new DummyCacheKey("tutu");
        // Put a content in cache...
        addCacheEntry(key, "content, yes again");
        Assert.assertThat(cache.has(key), is(true));
        // ... evict() it...
        cache.evict(key);
        // ... has() must immediately return false
        Assert.assertThat(cache.has(key), is(false));
    }


    @Test
    public void testJanitorShouldNotCleanAnything() throws Exception {

        // given some valid cache entries
        List<ContentCacheKey> keys = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            keys.add(new DummyCacheKey("do not disturb-" + i + 1));
        }

        for (ContentCacheKey key : keys) {
            addCacheEntry(key, "content");
            Assert.assertThat(cache.has(key), is(true));
        }

        // when the janitor is called
        cache.janitor();

        // then, none of the cache entries should be removed
        for (ContentCacheKey key : keys) {
            Assert.assertThat(cache.has(key), is(true));
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
            addCacheEntry(key, "janitor content");
            Assert.assertThat(cache.has(key), is(true));
        }

        // when eviction is performed and the janitor is called
        for (ContentCacheKey key : keys) {
            cache.evict(key);
        }
        cache.janitor();

        // then no file in the cache should be left
        Files.walkFileTree(Paths.get(TEST_DIRECTORY), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!StringUtils.contains(file.toFile().getName(), ".nfs")) {
                    Assert.fail("file " + file + " was not cleaned by the janitor");
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    /**
     * Add the cache entry.
     *
     * @param key where to put the cache entry.
     * @param content the cache entry content.
     * @throws IOException if an error occurs.
     */
    private void addCacheEntry(ContentCacheKey key, String content) throws IOException {
        try (OutputStream entry = cache.put(key, ContentCache.TimeToLive.DEFAULT)) {
            entry.write(content.getBytes());
            entry.flush();
        }
    }
}
