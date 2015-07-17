package org.talend.dataprep.dataset.store.content.file;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.exception.DataSetErrorCodes;
import org.talend.dataprep.dataset.store.content.DataSetContentStoreAdapter;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.TDPExceptionContext;

/**
 * Local dataset content that stores content in files.
 */
@Component("ContentStore#local")
@ConditionalOnProperty(name = "dataset.content.store", havingValue = "local", matchIfMissing = false)
public class LocalFileContentStore extends DataSetContentStoreAdapter {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileContentStore.class);

    @Value("${dataset.content.store.local.location}")
    private String storeLocation;

    @PostConstruct
    public void init() {
        if (storeLocation == null) {
            throw new IllegalArgumentException("Store location cannot be null.");
        }
        if (!storeLocation.endsWith("/")) { //$NON-NLS-1$
            storeLocation += "/"; //$NON-NLS-1$
        }
        LOGGER.info("Content store location: {}", storeLocation);
    }

    private File getFile(DataSetMetadata dataSetMetadata) {
        return new File(storeLocation + dataSetMetadata.getId());
    }

    @Override
    public void storeAsRaw(DataSetMetadata dataSetMetadata, InputStream dataSetContent) {
        try {
            if (dataSetContent.available() > 0) {
                File dataSetFile = getFile(dataSetMetadata);
                FileUtils.touch(dataSetFile);
                FileOutputStream fos = new FileOutputStream(dataSetFile);
                IOUtils.copy(dataSetContent, fos);
                LOGGER.debug("Data set #{} stored to '{}'.", dataSetMetadata.getId(), dataSetFile);
            } else {
                LOGGER.debug("Ignore update of data set #{} as content seems empty", dataSetMetadata.getId());
            }
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_STORE_DATASET_CONTENT, e, TDPExceptionContext.build().put("id",
                    dataSetMetadata.getId()));
        }
    }

    @Override
    public InputStream getAsRaw(DataSetMetadata dataSetMetadata) {
        try {
            return new FileInputStream(getFile(dataSetMetadata));
        } catch (FileNotFoundException e) {
            LOGGER.warn("File '{}' does not exist.", getFile(dataSetMetadata), e);
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public void delete(DataSetMetadata dataSetMetadata) {
        if (getFile(dataSetMetadata).exists()) {
            if (!getFile(dataSetMetadata).delete()) {
                throw new TDPException(DataSetErrorCodes.UNABLE_TO_DELETE_DATASET, TDPExceptionContext.build().put("dataSetId",
                        dataSetMetadata.getId()));
            }
        } else {
            LOGGER.warn("Data set #{} has no content.", dataSetMetadata.getId());
        }
    }

    @Override
    public void clear() {
        try {
            Path path = FileSystems.getDefault().getPath(storeLocation);
            if (!path.toFile().exists()) {
                return;
            }
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    // Skip NFS file content
                    if (!file.getFileName().toFile().getName().startsWith(".nfs")) { //$NON-NLS-1$
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    if (e == null) {
                        return FileVisitResult.CONTINUE;
                    } else {
                        // directory iteration failed
                        throw e;
                    }
                }
            });
        } catch (IOException e) {
            LOGGER.error("Unable to clear local data set content.", e);
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_CLEAR_DATASETS, e);
        }
    }

}
