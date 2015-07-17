package org.talend.dataprep.dataset.store.metadata.memory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.DistributedLock;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;

import com.google.common.base.Defaults;

@Component
@ConditionalOnProperty(name = "dataset.metadata.store", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryDataSetMetadataRepository implements DataSetMetadataRepository {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataSetMetadataRepository.class);

    private final Map<String, DataSetMetadata> store = new HashMap<>();

    @Autowired
    private ApplicationContext appcontext;

    @Override
    public Iterable<DataSetMetadata> list() {
        return store.values();
    }

    @Override
    public synchronized void add(DataSetMetadata dataSetMetadata) {
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
        }// else null so do nothing
    }

    @Override
    public void clear() {
        // Remove all data set (but use lock for remaining asynchronous processes).
        final List<DataSetMetadata> list = IteratorUtils.toList(list().iterator());
        for (DataSetMetadata metadata : list) {
            final DistributedLock lock = createDatasetMetadataLock(metadata.getId());
            try {
                lock.lock();
                remove(metadata.getId());
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public DataSetMetadata get(String id) {
        DataSetMetadata dataSetMetadata = store.get(id);
        resetTransientValues(dataSetMetadata);
        return dataSetMetadata;
    }

    @Override
    public void remove(String id) {
        store.remove(id);
    }

    @Override
    public DistributedLock createDatasetMetadataLock(String id) {
        return appcontext.getBean(DistributedLock.class, DATASET_LOCK_PREFIX + id);
    }
}
