package org.talend.dataprep.cache;

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
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@ConditionalOnProperty(name = "api.cache", havingValue = "file")
@EnableScheduling
public class FileSystemContentCache implements ContentCache {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemContentCache.class);

    private final String location;

    @Autowired
    public FileSystemContentCache(@Value("${api.cache.file.location}") String location) {
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
        String datasetId = key.getDatasetId();
        String preparationId = key.getPreparationId();
        String stepId = key.getStepId();
        String sample = key.getSample();
        if (datasetId == null) {
            throw new IllegalArgumentException("Dataset id cannot be null.");
        }
        if (preparationId == null) {
            throw new IllegalArgumentException("Preparation id cannot be null.");
        }
        if (key.getStepId() == null) {
            throw new IllegalArgumentException("Step id cannot be null.");
        }
        if ("origin".equalsIgnoreCase(stepId)) {
            stepId = Step.ROOT_STEP.id();
        }
        StringBuilder path = new StringBuilder(location);
        path.append("/datasets/") //
                .append(datasetId) //
                .append("/preparations/") //
                .append(preparationId) //
                .append("/steps/") //
                .append(stepId) //
                .append("/") //
                .append(sample);
        if (timeToLive != null) {
            path.append('.').append(String.valueOf(System.currentTimeMillis() + timeToLive.getTime()));
        }
        return Paths.get(path.toString());
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
                if (fileName.startsWith(key.getSample()) && Long.parseLong(suffix) > now) {
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
            // check the step id value
            String stepId = key.getStepId();
            if ("head".equals(stepId) || "origin".equals(stepId)) {
                throw new IllegalArgumentException("Illegal shortcut for preparation step '" + stepId + "'.");
            }
            final Path path = computeEntryPath(key, timeToLive);
            if (!path.toFile().exists()) {
                try {
                    FileUtils.touch(path.toFile());
                } catch (IOException e) {
                    LOGGER.error("[{} @{}] Unable to create cache.", key.getPreparationId(), key.getStepId(), e);
                }
            }
            LOGGER.debug("[{}] Cache add.", key);
            return Files.newOutputStream(path);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public void evict(ContentCacheKey key) {
        String temp = location + "/datasets";
        if (key.getDatasetId() != null) {
            temp += '/' + key.getDatasetId();
            if (key.getPreparationId() != null) {
                temp += "/preparations/" + key.getPreparationId();
                if (key.getStepId() != null) {
                    temp += "/steps/" + key.getStepId();
                }
            }
            // no need to go up to the sample size if set.
        }
        Path path = Paths.get(temp);
        // defensive programming
        if (!path.toFile().exists()) {
            return;
        }
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        final Path evictedFile = Paths.get(file.toAbsolutePath().toString() + ".0");
                        Files.move(file, evictedFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
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
        if (!Paths.get(location, "/datasets/").toFile().exists()) {
            LOGGER.debug("No cache content to clean.");
            return;
        }
        final long start = System.currentTimeMillis();
        final AtomicLong deletedCount = new AtomicLong();
        final AtomicLong totalCount = new AtomicLong();
        LOGGER.debug("Janitor process started @ {}.", start);
        try {
            Files.walkFileTree(Paths.get(location, "/datasets/"), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final String suffix = StringUtils.substringAfterLast(file.toFile().getName(), ".");
                    // Ignore NFS files (HDFS + NFS? yes, but may happen in local mode).
                    if (suffix.startsWith("nfs")) {
                        return FileVisitResult.CONTINUE;
                    }
                    final long time = Long.parseLong(StringUtils.isEmpty(suffix) ? "0" : suffix);
                    if (time < start) {
                        try {
                            Files.delete(file);
                            deletedCount.incrementAndGet();
                        } catch (NoSuchFileException e) {
                            System.out.println("\tNo such!");
                            LOGGER.debug("Ignored delete issue for '{}'.", file.getFileName(), e);
                        } catch (IOException e) {
                            LOGGER.error("Unable to delete '{}'.", file.getFileName(), e);
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
