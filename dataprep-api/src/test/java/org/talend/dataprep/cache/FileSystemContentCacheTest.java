package org.talend.dataprep.cache;

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
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;

public class FileSystemContentCacheTest {

    public static final String TEST_DIRECTORY = "/tmp/talend";

    private static final String DATASET_ID = "1234";

    private static final String AUTHOR = "Thor";

    private static final String NAME = "Mjöllnir";

    private static final String STEP_ID = "5678";

    private FileSystemContentCache cache;

    /** A default preparation ready to be used by the tests. */
    private Preparation preparation;

    /**
     * Default empty constructor.
     */
    public FileSystemContentCacheTest() {
        this.preparation = getPreparation(DATASET_ID, NAME, AUTHOR);
    }

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
        Assert.assertThat(cache.has(new ContentCacheKey(preparation, STEP_ID)), is(false));
    }

    @Test
    public void testPutHas() throws Exception {
        // Put a content in cache...
        ContentCacheKey key = new ContentCacheKey(preparation, STEP_ID);
        Assert.assertThat(cache.has(key), is(false));
        addCacheEntry(key, "content");
        // ... has() must return true
        Assert.assertThat(cache.has(key), is(true));
    }

    @Test
    public void testPutWithSampleSize() throws Exception {
        // Put a content in cache...
        ContentCacheKey key = new ContentCacheKey(preparation, STEP_ID, 25l);
        Assert.assertThat(cache.has(key), is(false));
        addCacheEntry(key, "content with sample");
        // ... has() must return true
        Assert.assertThat(cache.has(key), is(true));
    }

    @Test
    public void testPutOrigin() throws Exception {
        ContentCacheKey origin = new ContentCacheKey(preparation, "origin");
        ContentCacheKey key = new ContentCacheKey(preparation, Step.ROOT_STEP.id());
        // Put a content in cache...
        Assert.assertThat(cache.has(origin), is(false));
        Assert.assertThat(cache.has(key), is(false));
        addCacheEntry(key, "content");
        // ... has() must return true
        Assert.assertThat(cache.has(origin), is(true));
        Assert.assertThat(cache.has(key), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongPut_Head() throws Exception {
        // Put a content in cache with "head" is not accepted
        cache.put(new ContentCacheKey(preparation, "head"), ContentCache.TimeToLive.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongPut_Origin() throws Exception {
        // Put a content in cache with "origin" is not accepted
        cache.put(new ContentCacheKey(preparation, "origin"), ContentCache.TimeToLive.DEFAULT);
    }

    @Test
    public void testGet() throws Exception {
        ContentCacheKey key = new ContentCacheKey(preparation, STEP_ID);
        String content = "content";
        // Put a content in cache...
        addCacheEntry(key, content);
        // ... get() should return this content back.
        final String actual = IOUtils.toString(cache.get(key));
        Assert.assertThat(actual, is(content));
    }

    @Test
    public void testGetWithSampleSize() throws Exception {
        ContentCacheKey key = new ContentCacheKey(preparation, STEP_ID, 72L);
        String content = "content limited to 72 lines";
        // Put a content in cache...
        addCacheEntry(key, content);
        // ... get() should return this content back.
        final String actual = IOUtils.toString(cache.get(key));
        Assert.assertThat(actual, is(content));
    }

    @Test
    public void testEvict() throws Exception {
        ContentCacheKey key = new ContentCacheKey(preparation, STEP_ID, 43L);
        // Put a content in cache...
        addCacheEntry(key, "content");
        Assert.assertThat(cache.has(key), is(true));
        // ... evict() it...
        cache.evict(key);
        // ... has() must immediately return false
        Assert.assertThat(cache.has(key), is(false));
    }

    @Test
    public void testEvictAPreparation() throws Exception {
        List<ContentCacheKey> keys = new ArrayList<>();
        keys.add(new ContentCacheKey(preparation, STEP_ID, 43L));
        keys.add(new ContentCacheKey(preparation, STEP_ID + 123));
        keys.add(new ContentCacheKey(preparation, STEP_ID + 456, 22L));
        keys.add(new ContentCacheKey(preparation, STEP_ID + 789));

        // Put a content in cache...
        for (ContentCacheKey key : keys) {
            addCacheEntry(key, "content");
            Assert.assertThat(cache.has(key), is(true));
        }

        // evict the whole preparation
        final ContentCacheKey preparationKey = new ContentCacheKey(preparation, null);
        cache.evict(preparationKey);

        // ... has() must immediately return false
        for (ContentCacheKey key : keys) {
            Assert.assertThat(cache.has(key), is(false));
        }
    }

    @Test
    public void testEvictADataset() throws Exception {

        // 6 preparations with the same dataset
        List<ContentCacheKey> keys = new ArrayList<>();

        Preparation prep1 = getPreparation(DATASET_ID, "mark20", "Tony Stark");
        keys.add(new ContentCacheKey(prep1, STEP_ID, 43L));
        keys.add(new ContentCacheKey(prep1, STEP_ID + 123));

        Preparation prep2 = getPreparation(DATASET_ID, "Mjöllnir", "Thor");
        keys.add(new ContentCacheKey(prep2, STEP_ID + 753));
        keys.add(new ContentCacheKey(prep2, STEP_ID + 897, 78L));

        Preparation prep3 = getPreparation(DATASET_ID, "Smash", "Hulk");
        keys.add(new ContentCacheKey(prep3, STEP_ID + 852, 6000L));
        keys.add(new ContentCacheKey(prep3, STEP_ID + 249));

        // Put a content in cache...
        for (ContentCacheKey key : keys) {
            addCacheEntry(key, "content");
            Assert.assertThat(cache.has(key), is(true));
        }

        // evict the whole dataset
        final ContentCacheKey preparationKey = new ContentCacheKey(DATASET_ID);
        cache.evict(preparationKey);

        // ... has() must immediately return false
        for (ContentCacheKey key : keys) {
            Assert.assertThat(cache.has(key), is(false));
        }
    }

    @Test
    public void testJanitorShouldNotCleanAnything() throws Exception {

        // given some valid cache entries
        List<ContentCacheKey> keys = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            keys.add(new ContentCacheKey(preparation, STEP_ID + 1));
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

        // clean up the mess afterwards
        cache.evict(new ContentCacheKey(preparation, null));
    }

    @Test
    public void testJanitor() throws Exception {

        // given some cache entries
        List<ContentCacheKey> keys = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            keys.add(new ContentCacheKey(preparation, STEP_ID + 1));
        }
        for (ContentCacheKey key : keys) {
            addCacheEntry(key, "content");
            Assert.assertThat(cache.has(key), is(true));
        }

        // when eviction is performed and the janitor is called
        cache.evict(new ContentCacheKey(preparation, null));
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

    @Test(expected = IllegalArgumentException.class)
    public void testGetPath_nullStep() throws Exception {
        cache.has(new ContentCacheKey(preparation, null));
    }

    /**
     * Return a preparation from the given parameters.
     *
     * @param datasetId the dataset id.
     * @param author the preparation author.
     * @return a preparation from the given parameters.
     */
    private Preparation getPreparation(String datasetId, String name, String author) {
        Preparation preparation = new Preparation();
        preparation.setDataSetId(datasetId);
        preparation.setName(name);
        preparation.setAuthor(author);
        return preparation;
    }

    /**
     * ² Add the cache entry.
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
