/*
 *  ============================================================================
 *
 *  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 *  This source code is available under agreement available at
 *  https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 *  You should have received a copy of the agreement
 *  along with this program; if not, write to Talend SA
 *  9 rue Pages 92150 Suresnes, France
 *
 *  ============================================================================
 */

package org.talend.dataprep.dataset.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.service.DataSetService;
import org.talend.dataprep.util.SortAndOrderHelper;
import org.talend.dataprep.util.avro.AvroUtils;

import com.google.common.base.Throwables;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@RestController
@RequestMapping("/api/v1/datasets")
public class DataSetController {

    private final DataSetService dataSetService;

    private final BeanConversionService beanConversionService;

    public DataSetController(DataSetService dataSetService, BeanConversionService beanConversionService) {
        this.dataSetService = dataSetService;
        this.beanConversionService = beanConversionService;
    }

    @GetMapping
    public Stream<Dataset> getAllDatasets() {
        return dataSetService.list(SortAndOrderHelper.Sort.CREATION_DATE, SortAndOrderHelper.Order.DESC, null, false,
                false, false, false) //
                .map(userDataSetMetadata -> beanConversionService.convert(userDataSetMetadata, Dataset.class));
    }

    /**
     * Get dataset by id
     *
     * @param datasetId  id of the dataset
     * @param withUiSpec Add UISpec to the returned json
     * @param advanced   asks tcomp to add additionnal UISpec from the datastore
     * @return
     */
    @GetMapping("/{datasetId}")
    public Dataset getDataset(@PathVariable String datasetId,
            @RequestParam(required = false) boolean withUiSpec,
            @RequestParam(required = false) boolean advanced) {
        DataSet dataSet = dataSetService.getMetadata(datasetId);
        DataSetMetadata dataSetMetadata = dataSet.getMetadata();

        return beanConversionService.convert(dataSetMetadata, Dataset.class);
    }

    @GetMapping(value = "/{datasetId}/schema", produces = AvroUtils.AVRO_JSON_MIME_TYPES_UNOFFICIAL_VALID_VALUE)
    public String getDatasetSchema(@PathVariable String datasetId,
            @RequestParam(required = false) boolean withUiSpec,
            @RequestParam(required = false) boolean advanced) {
        DataSet dataSet = dataSetService.getMetadata(datasetId);
        RowMetadata rowMetadata = dataSet.getMetadata().getRowMetadata();
        return AvroUtils.toSchema(rowMetadata).toString();
    }

    @GetMapping(value = "/{datasetId}/content", produces = AvroUtils.AVRO_BINARY_MIME_TYPES_UNOFFICIAL_VALID_VALUE)
    public Resource getDatasetContent(@PathVariable String datasetId,
            @RequestParam(required = false) boolean withUiSpec,
            @RequestParam(required = false) boolean advanced) {
        InputStream result;
        Callable<DataSet> dataSetCallable = dataSetService.get(false, true, EMPTY, datasetId);
        Stream<DataSetRow> records;
        try {
            records = dataSetCallable.call().getRecords();
        } catch (Exception e) {
            Throwables.propagateIfPossible(e, RuntimeException.class);
            throw new RuntimeException("unexpected", e);
        }

        DataSetMetadata metadata = dataSetService.getMetadata(datasetId).getMetadata();
        Schema schema = AvroUtils.toSchema(metadata.getRowMetadata());
        GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);

            records.map(AvroUtils.toGenericRecordConverter(schema)) //
                    .forEach(record -> {
                        try {
                            writer.write(record, encoder);
                        } catch (IOException e) {
                            throw new TalendRuntimeException(BaseErrorCodes.UNEXPECTED_EXCEPTION, e);
                        }
                    });
            encoder.flush();
            result = new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new TalendRuntimeException(BaseErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
        return new InputStreamResource(result);
    }

}
