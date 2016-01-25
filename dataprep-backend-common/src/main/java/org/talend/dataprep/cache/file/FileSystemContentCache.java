package org.talend.dataprep.cache.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;

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

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemContentCache.class);

    /** Where to store cache entries. */
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
     * Compute the path for the given key.
     *
     * @param key the cache key entry.
     * @param timeToLive an optional time to live when performing content cache lookup (<code>null</code> allowed).
     * @return the HDFS path for the entry key.
     */
    private Path computeEntryPath(ContentCacheKey key, TimeToLive timeToLive) {
        String path = location + key.getKey();
        if (timeToLive != null) {
            path += '.' + String.valueOf(System.currentTimeMillis() + timeToLive.getTime());
        }
        return Paths.get(path);
    }

    @Override
    public boolean has(ContentCacheKey key) {
        final Path path = computeEntryPath(key, null);
        final File[] files = path.getParent().toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                final long now = System.currentTimeMillis();
                final String fileName = file.getName();
                final String suffix = StringUtils.substringAfterLast(fileName, ".");
                if (Long.parseLong(suffix) > now) {
                    LOGGER.debug("[{}] Cache hit.", key);
                    return true;
                }
            }
        }
        LOGGER.debug("[{}] Cache miss.", key);
        return false;
    }

    @Override
    public InputStream get(ContentCacheKey key) {
        final Path path = computeEntryPath(key, null);
        final File[] files = path.getParent().toFile().listFiles();
        if (files != null) {
            for (File file : files) {

                if (!StringUtils.startsWith(file.getName(), key.getKey())) {
                    continue;
                }

                final String suffix = StringUtils.substringAfterLast(file.getName(), ".");
                if (Long.parseLong(suffix) > System.currentTimeMillis()) {
                    try {
                        return Files.newInputStream(file.toPath());
                    } catch (IOException e) {
                        throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                    }
                }
            }
        }
        LOGGER.debug("No cache for entry #{}", key);
        return null;
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
            Files.walkFileTree(parent, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        if (StringUtils.startsWith(file.getFileName().toString(), key.getKey())) {
                            final Path evictedFile = Paths.get(file.toAbsolutePath().toString() + ".0");
                            Files.move(file, evictedFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                        }
                    } catch (IOException e) {
                        LOGGER.error("Unable to evict {}.", file.getFileName(), e);
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Unable to evict.", e);
        }
        LOGGER.debug("[{}] Evict.", key);
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
            Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>() {

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
                    final long time = Long.parseLong(StringUtils.isEmpty(suffix) ? "0" : suffix);
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
                    totalCount.incrementAndGet();
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Unable to clean up cache", e);
        }
        LOGGER.debug("Janitor process ended @ {} ({}/{} files successfully deleted).", System.currentTimeMillis(), deletedCount,
                totalCount);
    }

}
