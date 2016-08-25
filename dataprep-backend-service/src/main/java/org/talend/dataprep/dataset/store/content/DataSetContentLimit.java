package org.talend.dataprep.dataset.store.content;

public interface DataSetContentLimit {

    DataSetContentStore get(DataSetContentStore store);

    boolean limitContentSize();

}
