//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.dataset.store.metadata.memory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.dataset.store.metadata.ObjectDataSetMetadataRepository;

import com.google.common.base.Defaults;

/**
 * In memory implementation of the DataSetMetadataRepository.
 */
@Component
@ConditionalOnProperty(name = "dataset.metadata.store", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryDataSetMetadataRepository extends ObjectDataSetMetadataRepository {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataSetMetadataRepository.class);

    @Autowired
    private DataSetMetadataBuilder metadataBuilder;

    /** Where the DatasetMetadata is actually stored. */
    private final Map<String, DataSetMetadata> store = new ConcurrentHashMap<>();

    @Override
    public Stream<DataSetMetadata> source() {
        final Collection<DataSetMetadata> values = store.values();
        if (LOG.isDebugEnabled()) {
            LOG.debug("list {} dataset metadata", values.size());
        }
        return values.stream();
    }

    @Override
    public void save(DataSetMetadata dataSetMetadata) {
        store.put(dataSetMetadata.getId(), dataSetMetadata);
    }

    /**
     * this nullifies and resets transient values that are supposed not to be stored
     *
     * @param zeObject The object where non transient fields will be nullified.
     */
    void resetTransientValues(@Nullable Object zeObject) {
        if (zeObject != null) {
            Field[] fields = zeObject.getClass().getDeclaredFields();
            for (Field field : fields) {
                // ignore jacoco injected field
                if (Modifier.isTransient(field.getModifiers()) && !field.getName().endsWith("jacocoData")) { //$NON-NLS-1$
                    Object defaultValue = Defaults.defaultValue(field.getType());
                    field.setAccessible(true);
                    try {
                        field.set(zeObject, defaultValue);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        LOG.error("failed to reset the transient field [" + field + "] before storage", e);
                    }
                }
            }
        } // else null so do nothing
    }

    /**
     * @see DataSetMetadataRepository#get(String)
     */
    @Override
    public DataSetMetadata get(String id) {
        DataSetMetadata dataSetMetadata = store.get(id);
        if (dataSetMetadata == null) {
            LOG.info("data set metadata #{} not found in the system", id);
            return null;
        }
        resetTransientValues(dataSetMetadata);
        return metadataBuilder.metadata().copy(dataSetMetadata).build();
    }

    /**
     * @see DataSetMetadataRepository#remove(String)
     */
    @Override
    public void remove(String id) {
        store.remove(id);
    }

}
