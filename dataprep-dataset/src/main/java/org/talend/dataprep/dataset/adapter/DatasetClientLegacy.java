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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.service.DataSetService;
import org.talend.dataprep.util.SortAndOrderHelper;
import org.talend.dataprep.util.avro.AvroUtils;

import com.google.common.base.Throwables;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Dataset legacy implementation of {@link DatasetClient}
 */
@Service
public class DatasetClientLegacy implements DatasetClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetClientLegacy.class);

    private final DataSetService dataSetService;

    private final BeanConversionService beanConversionService;

    public DatasetClientLegacy(DataSetService dataSetService, BeanConversionService beanConversionService) {
        this.dataSetService = dataSetService;
        this.beanConversionService = beanConversionService;
    }

    @Override
    public Dataset findOne(String datasetId) {
        DataSet dataSet = dataSetService.getMetadata(datasetId);
        DataSetMetadata dataSetMetadata = dataSet.getMetadata();

        return beanConversionService.convert(dataSetMetadata, Dataset.class);
    }

    @Override
    public String findSchema(String datasetId) {
        DataSet dataSet = dataSetService.getMetadata(datasetId);
        Schema schema = AvroUtils.toSchema(dataSet.getMetadata().getRowMetadata());
        return schema.toString();
    }

    @Override
    public InputStream findBinaryAvroData(String datasetId, PageRequest pageRequest) {
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
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new TalendRuntimeException(BaseErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public List<Dataset> findAll() {
        return dataSetService.list(SortAndOrderHelper.Sort.CREATION_DATE, SortAndOrderHelper.Order.DESC, null, false,
                false, false, false) //
                .map(userDataSetMetadata -> beanConversionService.convert(userDataSetMetadata, Dataset.class)) //
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Dataset entity) {
        throw new UnsupportedOperationException();
    }
}
