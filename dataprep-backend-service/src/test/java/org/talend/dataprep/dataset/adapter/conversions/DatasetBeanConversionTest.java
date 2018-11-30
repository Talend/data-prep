package org.talend.dataprep.dataset.adapter.conversions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DatasetDTO;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.schema.Schema;
import org.talend.dataprep.schema.csv.CSVFormatFamily;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatasetBeanConversionTest {

    private BeanConversionService beanConversionService = new BeanConversionService();

    private ObjectMapper objectMapper = new ObjectMapper();

    private Dataset dataset = new Dataset();

    @Before
    public void setUp() throws IOException {
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        // conversion registration
        new DatasetBeanConversion(objectMapper).doWith(beanConversionService, "toto", null);
        // create catalog dataset from json file
        dataset = objectMapper.readValue(getClass().getResourceAsStream("../dataset_payload_example.json"),
                Dataset.class);
    }

    @Test
    public void datasetToDatasetDTOConversion() {
        // when
        DatasetDTO datasetDTO = beanConversionService.convert(dataset, DatasetDTO.class);

        // then
        assertEquals(dataset.getId(), datasetDTO.getId());
        assertEquals(dataset.getCreated(), datasetDTO.getCreationDate());
        assertEquals(dataset.getUpdated(), datasetDTO.getLastModificationDate());
        assertEquals(dataset.getLabel(), datasetDTO.getName());
        assertEquals(dataset.getOwner(), datasetDTO.getAuthor());
        assertEquals(CSVFormatFamily.MEDIA_TYPE, datasetDTO.getType());
        assertEquals(dataset.getCertification(), datasetDTO.getCertification());
    }

    @Test
    @Ignore
    public void defaultDatasetToDataSetMetadataConversion() {
        // when
        DataSetMetadata dataSetMetadata = beanConversionService.convert(dataset, DataSetMetadata.class);

        // then
        assertEquals(dataset.getId(), dataSetMetadata.getId());
        assertEquals(dataset.getCreated(), (Long) dataSetMetadata.getCreationDate());
        assertEquals(dataset.getUpdated(), (Long) dataSetMetadata.getLastModificationDate());
        assertEquals(dataset.getLabel(), dataSetMetadata.getName());
        assertEquals(dataset.getOwner(), dataSetMetadata.getAuthor());
    }

    @Test
    @Ignore
    public void datasetToDataSetMetadataConversionWithLegacy() {
        // given
        // manage legacy DataSetMetadata fields that don't match the Dataset Catalog model
        Dataset.DataSetMetadataLegacy dataSetMetadataLegacy = new Dataset.DataSetMetadataLegacy();
        dataSetMetadataLegacy.setDraft(true);
        dataSetMetadataLegacy.setSheetName("xls-sheet-name");
        dataSetMetadataLegacy.setEncoding(StandardCharsets.UTF_8.name());
        dataSetMetadataLegacy.setTag("tag-from-studio");

        Schema.Builder builder = new Schema.Builder();
        dataSetMetadataLegacy.setSchemaParserResult(builder.build());

        dataset.setDataSetMetadataLegacy(dataSetMetadataLegacy);

        // when
        DataSetMetadata dataSetMetadata = beanConversionService.convert(dataset, DataSetMetadata.class);

        // then
        assertEquals(dataset.getId(), dataSetMetadata.getId());
        assertEquals(dataset.getCreated(), (Long) dataSetMetadata.getCreationDate());
        assertEquals(dataset.getUpdated(), (Long) dataSetMetadata.getLastModificationDate());
        assertEquals(dataset.getLabel(), dataSetMetadata.getName());
        assertEquals(dataset.getOwner(), dataSetMetadata.getAuthor());

        assertEquals(dataSetMetadataLegacy.getSheetName(), dataSetMetadata.getSheetName());
        assertNotNull(dataSetMetadata.getSchemaParserResult());
        assertEquals(dataSetMetadataLegacy.isDraft(), dataSetMetadata.isDraft());
        assertEquals(dataSetMetadataLegacy.getEncoding(), dataSetMetadata.getEncoding());
        assertEquals(dataSetMetadataLegacy.getTag(), dataSetMetadata.getTag());
    }
}
