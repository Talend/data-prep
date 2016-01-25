package org.talend.dataprep.dataset.store.metadata.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepositoryAdapter;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.util.ReentrantReadWriteLockGroup;

/**
 * File system implementation of the DataSetMetadataRepository.
 *
 * DatasetMetadata are saved as gzipped json files.
 */
@Component
@ConditionalOnProperty(name = "dataset.metadata.store", havingValue = "file")
public class FileSystemDataSetMetadataRepository extends DataSetMetadataRepositoryAdapter {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemDataSetMetadataRepository.class);

    /** The dataprep ready jackson builder. */
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    /** Where to store the dataset metadata */
    @Value("${dataset.metadata.store.file.location}")
    private String storeLocation;

    @PostConstruct
    private void init() {
        getRootFolder().mkdirs();
    }

    /**
     * A group of ReentrantReadWriteLock associating to each dataset id a unique ReentrantReadWriteLock.
     */
    private final ReentrantReadWriteLockGroup locks = new ReentrantReadWriteLockGroup(true, 100);

    /**
     * @see DataSetMetadataRepository#add(DataSetMetadata)
     */
    @Override
    public void add(DataSetMetadata metadata) {

        String id = metadata.getId();

        ReentrantReadWriteLock lock = locks.getLock(id);
        final File file = getFile(id);

        lock.writeLock().lock();
        try (GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
            builder.build().writer().writeValue(output, metadata);
        } catch (IOException e) {
            LOG.error("Error saving {}", metadata, e);
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_STORE_DATASET_METADATA, e,
                    ExceptionContext.build().put("id", metadata.getId()));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @see DataSetMetadataRepository#get(String)
     */
    @Override
    public DataSetMetadata get(String id) {

        final File file = getFile(id);
        if (file.getName().startsWith(".")) {
            LOG.info("Ignore hidden file {}", file.getName());
            return null;
        }
        if (!file.exists()) {
            LOG.info("dataset #{} not found in file system", id);
            return null;
        }
        ReentrantReadWriteLock lock = locks.getLock(id);

        lock.readLock().lock();
        try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(file))) {
            return builder.build().readerFor(DataSetMetadata.class).readValue(input);
        } catch (IOException e) {
            LOG.error("unable to load dataset {}", id, e);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @see DataSetMetadataRepository#remove(String)
     */
    @Override
    public void remove(String id) {
        final File file = getFile(id);
        if (file.exists() && !isNFSSpecificFile(file)) {
            file.delete();
        }
        LOG.debug("metadata {} successfully deleted", id);
    }

    /**
     * @see DataSetMetadataRepository#size()
     */
    @Override
    public int size() {
        File folder = getRootFolder();
        int size = folder.list().length;
        LOG.debug("found {} datasets metadata", size);
        return size;
    }

    /**
     * @see DataSetMetadataRepository#list()
     */
    @Override
    public Iterable<DataSetMetadata> list() {
        File folder = getRootFolder();
        final File[] files = folder.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        final Stream<DataSetMetadata> stream = Arrays.stream(files).map(f -> get(f.getName())).filter(m -> m != null);
        return stream::iterator;
    }

    /**
     * @see DataSetMetadataRepository#clear()
     */
    @Override
    public void clear() {
        // Remove all data set (but use lock for remaining asynchronous processes).
        for (DataSetMetadata metadata : list()) {
            if (metadata == null) {
                continue;
            }
            final DistributedLock lock = createDatasetMetadataLock(metadata.getId());
            try {
                lock.lock();
                remove(metadata.getId());
            } finally {
                lock.unlock();
            }
        }
        LOG.debug("dataset metadata repository cleared.");
    }

    /**
     * Return the file that matches the given metadata id.
     * 
     * @param metadataId the metadata id.
     * @return the file where to read/write the metadata.
     */
    private File getFile(String metadataId) {
        return new File(storeLocation + '/' + metadataId);
    }

    /**
     * @return the metadata root folder.
     */
    private File getRootFolder() {
        return new File(storeLocation + '/');
    }

    /**
     * Specific OS NFS files must be not be deleted.
     * 
     * @param file the file to read.
     * @return True if the given belongs to the operating system and is NFS related (starts with ".nfs")
     */
    private boolean isNFSSpecificFile(File file) {
        return file.getName().startsWith(".nfs");
    }

}
