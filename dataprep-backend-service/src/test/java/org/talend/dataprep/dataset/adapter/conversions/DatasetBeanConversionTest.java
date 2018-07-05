package org.talend.dataprep.dataset.adapter.conversions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.dataset.adapter.Datastore;

import static org.junit.Assert.*;

public class DatasetBeanConversionTest {

    private BeanConversionService beanConversionService = new BeanConversionService();

    @Before
    public void registerBean() {
        new DatasetBeanConversion(new ObjectMapper()).doWith(beanConversionService, "toto", null);
    }

    @Test
    public void doWith() {
        Dataset base = new Dataset();
        base.setId("123456");
        base.setCreated(System.currentTimeMillis());
        base.setUpdated(System.currentTimeMillis());
        base.setDatastore(new Datastore());

        // then
        DataSetMetadata result = beanConversionService.convert(base, DataSetMetadata.class);

        assertEquals(base.getId(), result.getId());
        assertEquals(base.getCreated(), (Long) result.getCreationDate());
        assertEquals(base.getUpdated(), (Long) result.getLastModificationDate());
    }
}
