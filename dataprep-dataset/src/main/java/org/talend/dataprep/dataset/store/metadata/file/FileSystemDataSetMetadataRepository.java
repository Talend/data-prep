// ============================================================================
//
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

package org.talend.dataprep.dataset.store.metadata.file;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
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
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.dataset.store.metadata.ObjectDataSetMetadataRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.util.FilesHelper;
import org.talend.dataprep.util.ReentrantReadWriteLockGroup;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * File system implementation of the DataSetMetadataRepository.
 *
 * DatasetMetadata are saved as gzipped json files.
 */
@Component
@ConditionalOnProperty(name = "dataset.metadata.store", havingValue = "file")
public class FileSystemDataSetMetadataRepository extends ObjectDataSetMetadataRepository {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemDataSetMetadataRepository.class);

    /**
     * A group of ReentrantReadWriteLock associating to each dataset id a unique ReentrantReadWriteLock.
     */
    private final ReentrantReadWriteLockGroup locks = new ReentrantReadWriteLockGroup(true, 100);

    /** The dataprep ready jackson builder. */
    @Autowired
    private ObjectMapper mapper;

    /** Where to store the dataset metadata */
    @Value("${dataset.metadata.store.file.location}")
    private String storeLocation;

    @PostConstruct
    private void init() {
        try {
            Files.createDirectories(getRootFolder().toPath());
        } catch (IOException e) {
            throw new IllegalStateException("unable to create dataset metadata store folder", e);
        }
    }

    @Override
    public void save(DataSetMetadata metadata) {

        String id = metadata.getId();

        ReentrantReadWriteLock lock = locks.getLock(id);
        final File file = getFile(id);

        lock.writeLock().lock();
        try (GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
            mapper.writer().writeValue(output, metadata);
        } catch (IOException e) {
            LOG.error("Error saving {}", metadata, e);
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_STORE_DATASET_METADATA, e,  ExceptionContext.build().put("id", metadata.getId()));
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
        try (FileInputStream fileInputStream = new FileInputStream(file);
                GZIPInputStream input = new GZIPInputStream(fileInputStream)) {
            return mapper.readerFor(DataSetMetadata.class).readValue(input);
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
        FilesHelper.deleteQuietly(file);
        LOG.debug("metadata {} successfully deleted", id);
    }

    @Override
    public Stream<DataSetMetadata> source() {
        final File folder = getRootFolder();
        final File[] files = folder.listFiles();
        if (files == null) {
            return Stream.empty();
        }

        return Arrays.stream(files).map(f -> get(f.getName())).filter(m -> m != null);
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

}
