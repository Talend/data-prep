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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.row.RowMetadataUtils;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.service.DataSetService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

/**
 * Dataprep implementation of {@link DatasetClient}
 */
public class DataprepDatasetClient implements DatasetClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataprepDatasetClient.class);

    private final DataSetService dataSetService;

    private final BeanConversionService beanConversionService;

    private final ObjectMapper objectMapper;

    public DataprepDatasetClient(DataSetService dataSetService, BeanConversionService beanConversionService,
            ObjectMapper objectMapper) {
        this.dataSetService = dataSetService;
        this.beanConversionService = beanConversionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Dataset findOne(String datasetId) {
        DataSet dataSet = dataSetService.getMetadata(datasetId);
        DataSetMetadata dataSetMetadata = dataSet.getMetadata();

        return beanConversionService.convert(dataSetMetadata, Dataset.class);
    }

    @Override
    public ObjectNode findSchema(String datasetId) {
        DataSet dataSet = dataSetService.getMetadata(datasetId);
        Schema schema = RowMetadataUtils.toSchema(dataSet.getMetadata().getRowMetadata());

        try {
            return (ObjectNode) objectMapper.readTree(schema.toString());
        } catch (IOException e) {
            throw new TalendRuntimeException(BaseErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public InputStream findData(String datasetId, PageRequest pageRequest) {
        Callable<DataSet> dataSetCallable = dataSetService.get(false, true, null, datasetId);
        DataSet dataSet;
        try {
            dataSet = dataSetCallable.call();
        } catch (Exception e) {
            Throwables.propagateIfPossible(e, RuntimeException.class);
            throw new RuntimeException("unexpected", e);
        }

        Schema schema = RowMetadataUtils.toSchema(dataSet.getMetadata().getRowMetadata());

        //Json.ObjectWriter objectWriter = new Json.ObjectWriter();
        GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JsonEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(schema, outputStream);

            dataSet.getRecords().map(RowMetadataUtils::toRecord)
                    .map(RowMetadataUtils.Record::getIndexedRecord)
                    .forEach(indexedRecord -> {
                try {
                    writer.write(indexedRecord, jsonEncoder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            jsonEncoder.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    @Override
    public List<Dataset> findAll() {
        return Collections.emptyList();
    }

    @Override
    public boolean exists(String id) {
        return false;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public void delete(Dataset entity) {

    }
}
