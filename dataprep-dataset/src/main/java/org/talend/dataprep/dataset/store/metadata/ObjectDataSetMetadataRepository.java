// ============================================================================
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

package org.talend.dataprep.dataset.store.metadata;

import static org.talend.dataprep.util.SortAndOrderHelper.getDataSetMetadataComparator;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;
import org.talend.tql.bean.BeanPredicateVisitor;
import org.talend.tql.parser.Tql;

public abstract class ObjectDataSetMetadataRepository extends DataSetMetadataRepositoryAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectDataSetMetadataRepository.class);

    protected abstract Stream<DataSetMetadata> source();

    @Override
    public boolean exist(String filter) {
        final Predicate<DataSetMetadata> accept = Tql.parse(filter).accept(new BeanPredicateVisitor<>(DataSetMetadata.class));
        return source().anyMatch(accept);
    }

    @Override
    public Stream<DataSetMetadata> list() {
        return source();
    }

    @Override
    public Stream<DataSetMetadata> list(String filter, Sort sortField, Order sortDirection) {
        final Predicate<DataSetMetadata> accept = Tql.parse(filter).accept(new BeanPredicateVisitor<>(DataSetMetadata.class));
        final Stream<DataSetMetadata> stream = source().filter(accept);
        if (sortField != null) {
            final Comparator<DataSetMetadata> dataSetMetadataComparator = getDataSetMetadataComparator(sortField, sortDirection);
            return stream.sorted(dataSetMetadataComparator);
        } else {
            return stream;
        }
    }

    @Override
    public int size() {
        return (int) source().count();
    }

    @Override
    public void clear() {
        // Remove all data set (but use lock for remaining asynchronous processes).
        list().forEach(m -> {
            if (m != null) {
                final DistributedLock lock = createDatasetMetadataLock(m.getId());
                try {
                    lock.lock();
                    remove(m.getId());
                } finally {
                    lock.unlock();
                }
            }
        });
        LOGGER.debug("dataset metadata repository cleared.");
    }

    @Override
    public long countAllDataSetsSize() {
        return list().mapToLong(DataSetMetadata::getDataSetSize).sum();
    }

}
