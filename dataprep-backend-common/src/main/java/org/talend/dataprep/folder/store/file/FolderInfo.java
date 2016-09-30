package org.talend.dataprep.folder.store.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

class FolderInfo {

    /**
     * This class' logger.
     */
    private static final Logger LOG = getLogger(FolderInfo.class);
    /**
     * Folder last modification date.
     */
    private final long lastModificationDate;
    /**
     * Folder creation date.
     */
    private final long creationDate;

    /**
     * Constructor.
     *
     * @param lastModificationDate the folder last modification date.
     * @param creationDate         the folder creation date.
     */
    FolderInfo(long lastModificationDate, long creationDate) {
        this.lastModificationDate = lastModificationDate;
        this.creationDate = creationDate;
    }

    public long getLastModificationDate() {
        return lastModificationDate;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public static FolderInfo create(Path path) {
        FolderInfo result = null;
        BasicFileAttributes attributes = null;
        try {
            attributes = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            LOG.debug("cannot read file attributes {}", path, e);
        }
        if (attributes != null) {
            result = new FolderInfo(attributes.creationTime().to(TimeUnit.MILLISECONDS),
                    attributes.lastModifiedTime().to(TimeUnit.MILLISECONDS));
        }

        return result;
    }

    @Override
    public String toString() {
        return "FolderInfo{" +
                "lastModificationDate=" + lastModificationDate +
                ", creationDate=" + creationDate +
                '}';
    }
}
