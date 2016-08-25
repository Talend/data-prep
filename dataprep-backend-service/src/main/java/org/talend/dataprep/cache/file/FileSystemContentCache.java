// ============================================================================
//
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

package org.talend.dataprep.cache.file;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * File system cache implementation.
 */
@Component
@ConditionalOnProperty(name = "service.cache", havingValue = "file")
@EnableScheduling
public class FileSystemContentCache implements ContentCache {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemContentCache.class);

    /**
     * Where to store cache entries.
     */
    private final String location;

    /**
     * Default constructor.
     *
     * @param location where to store cache entries.
     */
    @Autowired
    public FileSystemContentCache(@Value("${service.cache.file.location}") String location) {
        if (!location.endsWith("/")) {
            location += "/";
        }
        this.location = location + "cache/";
        try {
            final File directory = new File(location);
            if (!directory.exists()) {
                FileUtils.forceMkdir(directory);
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Checks if the timeToLive (TTL) of a cache entry is ok for the cache entry.
     *
     * @param timeToLive The TTL of the cache entry.
     * @return <code>true</code> is TTL is greater than current time (+ {@link #EVICTION_PERIOD}), <code>false</code>
     * otherwise (or if time to live is not a number).
     */
    private static boolean isLiveEntry(String timeToLive) {
        // deal with permanent content
        if (StringUtils.isBlank(timeToLive)) {
            return true;
        }

        try {
            return Long.parseLong(timeToLive) > (System.currentTimeMillis() + EVICTION_PERIOD);
        } catch (NumberFormatException e) {
            LOGGER.debug("Invalid time to live '{}', consider entry as invalid.", timeToLive, e);
            return false;
        }
    }

    /**
     * Compute the path for the given key.
     *
     * @param key the cache key entry.
     * @param timeToLive an optional time to live when performing content cache lookup (<code>null</code> allowed).
     * @return the HDFS path for the entry key.
     */
    private Path computeEntryPath(ContentCacheKey key, TimeToLive timeToLive) {
        String path = location + key.getKey();
        if (timeToLive != null) {
            if (timeToLive.getTime() > 0) {
                path += '.' + String.valueOf(System.currentTimeMillis() + timeToLive.getTime());
            } else {
                // Leave path as it is (don't add timestamp).
            }
        }
        final Path result = Paths.get(path);
        LOGGER.trace("path entry for {} is {}", key.getKey(), result);
        return result;
    }

    private Path findEntry(ContentCacheKey key) {
        final Path path = computeEntryPath(key, null);
        final File[] files = path.getParent().toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (!StringUtils.startsWith(file.getName(), key.getKey())) {
                    LOGGER.trace("file {} does not match key {}", file.getName(), key.getKey());
                    continue;
                }
                if (Paths.get(file.toURI()).equals(path.toAbsolutePath())) {
                    LOGGER.debug("cache entry for #{} is {}", key, file.toPath());
                    return file.toPath();
                }
                final String suffix = StringUtils.substringAfterLast(file.getName(), ".");
                if (isLiveEntry(suffix)) {
                    LOGGER.debug("cache entry for #{} is {}", key, file.toPath());
                    return file.toPath();
                }
            }
        }
        LOGGER.debug("No cache for entry #{}", key);
        return null;
    }

    @Override
    public boolean has(ContentCacheKey key) {
        final Path path = computeEntryPath(key, null);
        final File[] files = path.getParent().toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                final String fileName = file.getName();
                // first check the key...
                final String prefix = StringUtils.substringBeforeLast(fileName, ".");
                if (StringUtils.equals(prefix, key.getKey())) {
                    // ...then the TTL
                    final String suffix = StringUtils.substringAfterLast(fileName, ".");
                    if (isLiveEntry(suffix)) {
                        LOGGER.debug("[{}] Cache hit --> {}", key, fileName);
                        return true;
                    }
                }
            }
        }
        LOGGER.debug("[{}] Cache miss.", key);
        return false;
    }

    @Override
    public InputStream get(ContentCacheKey key) {
        final Path path = findEntry(key);
        if (path == null) {
            LOGGER.debug("No cache for entry #{}", key);
            return null;
        }
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public OutputStream put(ContentCacheKey key, TimeToLive timeToLive) {
        try {
            final Path path = computeEntryPath(key, timeToLive);
            if (!path.toFile().exists()) {
                try {
                    FileUtils.touch(path.toFile());
                } catch (IOException e) {
                    LOGGER.error("{} Unable to create cache.", key, e);
                }
            }
            LOGGER.debug("{} Cache add.", key);
            return Files.newOutputStream(path);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public void evict(ContentCacheKey key) {

        final Path path = computeEntryPath(key, null);
        final Path parent = path.getParent();

        // defensive programming
        if (!parent.toFile().exists()) {
            return;
        }
        try {
            final String keyStr = key.getKey();
            final BiConsumer<Path, String> evictKey = getEvictionConsumer((entryKey) -> StringUtils.startsWith(entryKey, keyStr));
            final boolean skipPermanentEntries = false;
            Files.walkFileTree(Paths.get(location), new FileSystemVisitor(evictKey, skipPermanentEntries));
        } catch (IOException e) {
            LOGGER.error("Unable to evict.", e);
        }
        LOGGER.debug("[{}] Evict.", key);
    }

    @Override
    public void evictMatch(ContentCacheKey key) {
        final Path path = computeEntryPath(key, null);
        final Path parent = path.getParent();

        // defensive programming
        if (!parent.toFile().exists()) {
            return;
        }

        try {
            final Predicate<String> matchKey = key.getMatcher();
            final BiConsumer<Path, String> evictKey = getEvictionConsumer(matchKey);
            final boolean skipPermanentEntries = false;
            Files.walkFileTree(Paths.get(location), new FileSystemVisitor(evictKey, skipPermanentEntries));
        } catch (IOException e) {
            LOGGER.error("Unable to evict.", e);
        }
        LOGGER.debug("[{}] Evict Match.", key);
    }

    @Override
    public void move(ContentCacheKey from, ContentCacheKey to, TimeToLive toTimeToLive) {
        if (StringUtils.equals(from.getKey(), to.getKey())) {
            return; // Move to itself -> no op.
        }
        try {
            final Path fromPath = findEntry(from);
            if (fromPath == null) {
                LOGGER.warn("Cache entry '{}' cannot be found to be moved.", from.getKey());
                return;
            }
            final Path toPath = computeEntryPath(to, toTimeToLive);
            Files.move(fromPath, toPath, REPLACE_EXISTING, ATOMIC_MOVE);
            evict(from);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public void clear() {
        try {
            FileUtils.deleteDirectory(Paths.get(location).toFile());
        } catch (IOException e) {
            LOGGER.error("Unable to clear cache.", e);
        }
    }

    /**
     * A clean up process that starts a minute after the previous ended.
     */
    @Scheduled(fixedDelay = 60000)
    public void janitor() {
        if (!Paths.get(location).toFile().exists()) {
            LOGGER.debug("No cache content to clean.");
            return;
        }
        final long start = System.currentTimeMillis();
        final AtomicLong deletedCount = new AtomicLong();
        final AtomicLong totalCount = new AtomicLong();
        LOGGER.debug("Janitor process started @ {}.", start);
        try {
            final BiConsumer<Path, String> deleteOld = (file, suffix) -> {
                try {
                    final long time = Long.parseLong(suffix);
                    if (time < start) {
                        try {
                            Files.delete(file);
                            deletedCount.incrementAndGet();
                        } catch (NoSuchFileException e) {
                            LOGGER.debug("Ignored delete issue for '{}'.", file.getFileName(), e);
                        } catch (IOException e) {
                            LOGGER.warn("Unable to delete '{}'.", file.getFileName());
                            LOGGER.debug("Unable to delete '{}'.", file.getFileName(), e);
                        }
                    }
                } catch (NumberFormatException e) {
                    LOGGER.debug("Ignore file '{}'", file);
                }
                totalCount.incrementAndGet();
            };
            Files.walkFileTree(Paths.get(location), new FileSystemVisitor(deleteOld));
        } catch (IOException e) {
            LOGGER.error("Unable to clean up cache", e);
        }
        LOGGER.debug("Janitor process ended @ {} ({}/{} files successfully deleted).", System.currentTimeMillis(), deletedCount,
                totalCount);
    }

    private BiConsumer<Path, String> getEvictionConsumer(final Predicate<String> keyMatcher) {
        return (file, suffix) -> {
            try {
                if (keyMatcher.test(file.getFileName().toString())) {
                    final Path evictedFile = Paths.get(file.toAbsolutePath().toString() + ".0");
                    Files.move(file, evictedFile, REPLACE_EXISTING, ATOMIC_MOVE);
                }
            } catch (IOException e) {
                LOGGER.error("Unable to evict {}.", file.getFileName(), e);
            }
        };
    }

    private class FileSystemVisitor extends SimpleFileVisitor<Path> {

        private final BiConsumer<Path, String> consumer;

        private final boolean skipPermanent;

        FileSystemVisitor(final BiConsumer<Path, String> consumer) {
            this(consumer, true);
        }

        FileSystemVisitor(final BiConsumer<Path, String> consumer, final boolean skipPermanent) {
            this.consumer = consumer;
            this.skipPermanent = skipPermanent;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final String fileName = file.toFile().getName();
            // Ignore "." files (hidden files like MacOS).
            if (fileName.startsWith(".")) {
                return FileVisitResult.CONTINUE;
            }
            final String suffix = StringUtils.substringAfterLast(fileName, ".");
            // Ignore NFS files (may happen in local mode when NFS is used).
            if (suffix.startsWith("nfs")) {
                return FileVisitResult.CONTINUE;
            }
            if (this.skipPermanent && StringUtils.isEmpty(suffix)) {
                return FileVisitResult.CONTINUE;
            }
            consumer.accept(file, suffix);
            return super.visitFile(file, attrs);
        }
    }

}
