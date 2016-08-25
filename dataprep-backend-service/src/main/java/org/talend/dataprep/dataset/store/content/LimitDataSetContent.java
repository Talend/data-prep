package org.talend.dataprep.dataset.store.content;

import java.io.InputStream;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

@Component
class LimitDataSetContent implements DataSetContentLimit {

    @Value("${dataset.records.limit:10000}")
    private long limit;

    @Override
    public DataSetContentStore get(DataSetContentStore contentStore) {
        return new LimitDataSetContentStore(contentStore);
    }

    @Override
    public boolean limitContentSize() {
        return true;
    }

    private class LimitDataSetContentStore extends DataSetContentStore {

        private final DataSetContentStore delegate;

        private LimitDataSetContentStore(DataSetContentStore store) {
            this.delegate = store;
        }

        @Override
        public void storeAsRaw(DataSetMetadata dataSetMetadata, InputStream dataSetContent) {
            delegate.storeAsRaw(dataSetMetadata, dataSetContent);
        }

        @Override
        public InputStream get(DataSetMetadata dataSetMetadata) {
            return delegate.get(dataSetMetadata, limit);
        }

        @Override
        public Stream<DataSetRow> stream(DataSetMetadata dataSetMetadata, long limit) {
            return delegate.stream(dataSetMetadata, LimitDataSetContent.this.limit);
        }

        @Override
        public Stream<DataSetRow> stream(DataSetMetadata dataSetMetadata) {
            Stream<DataSetRow> dataSetRowStream = delegate.stream(dataSetMetadata, limit);
            // deal with dataset size limit (ignored if limit is <= 0)
            return dataSetRowStream.limit(limit);
        }

        @Override
        public InputStream getAsRaw(DataSetMetadata dataSetMetadata) {
            return delegate.getAsRaw(dataSetMetadata, limit);
        }

        @Override
        public InputStream getAsRaw(DataSetMetadata dataSetMetadata, long limit) {
            return delegate.getAsRaw(dataSetMetadata);
        }

        @Override
        protected InputStream get(DataSetMetadata dataSetMetadata, long limit) {
            return delegate.get(dataSetMetadata);
        }

        @Override
        public void delete(DataSetMetadata dataSetMetadata) {
            delegate.delete(dataSetMetadata);
        }

        @Override
        public void clear() {
            delegate.clear();
        }
    }
}
