package org.talend.dataprep.dataset.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetContent;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetMetadata;
import org.talend.dataprep.dataset.adapter.commands.DataSetGetSchema;
import org.talend.dataprep.dataset.adapter.commands.DatasetList;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.util.avro.AvroUtils;

import java.io.InputStream;
import java.util.stream.Stream;

import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

/**
 * Client based on Hystrix commands to call a dataset API. Exposes native avro calls and conversions.
 */
// It also allows to avoid using context.getBean everywhere
@Service
public class ApiDatasetClient {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DataSetMetadataBuilder dataSetMetadataBuilder;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AnalyzerService analyzerService;

    public Stream<Dataset> listDataset() {
        return context.getBean(DatasetList.class).execute();
    }

    public Dataset getMetadata(String id) {
        return context.getBean(DataSetGetMetadata.class, id).execute();
    }

    public DataSetMetadata getDataSetMetadata(String id) {
        Dataset dataset = getMetadata(id);
        RowMetadata rowMetadata = getDataSetRowMetadata(id);

        DataSetMetadata metadata = dataSetMetadataBuilder.metadata() //
                .id(dataset.getId()) //
                .name(dataset.getLabel()) //
                .created(dataset.getCreated()) //
                .modified(dataset.getUpdated()) //
                .build();
        metadata.setRowMetadata(rowMetadata);
        return metadata;
    }

    public Schema getDataSetSchema(String id) {
        return context.getBean(DataSetGetSchema.class, id).execute();
    }

    public RowMetadata getDataSetRowMetadata(String id) {
        Schema dataSetSchema = getDataSetSchema(id);
        return AvroUtils.toRowMetadata(dataSetSchema);
    }

    public Stream<GenericRecord> getDataSetContent(String id) {
        Schema schema = getDataSetSchema(id);
        return context.getBean(DataSetGetContent.class, id, schema).execute();
    }

    public Stream<DataSetRow> getDataSetContentAsRows(String id, RowMetadata rowMetadata) {
        return getDataSetContent(id).map(AvroUtils.toDataSetRowConverter(rowMetadata));
    }

    public Stream<DataSetRow> getDataSetContentAsRows(String id) {
        DataSetMetadata metadata = getDataSetMetadata(id);
        return getDataSetContent(id).map(AvroUtils.toDataSetRowConverter(metadata.getRowMetadata()));
    }

    public DataSet getDataSet(String id) {
        return getDataSet(id, null);
    }

    public DataSet getDataSet(String id, String preparationId) {
        DataSet dataset = new DataSet();
        DataSetMetadata dataSetMetadata = getDataSetMetadata(id);

        // If we do not have statistics. Ugly but efficient...
        // correct solution would be to refactor half dataprep strategies and API...
        if (dataSetMetadata.getRowMetadata().getColumns().stream().anyMatch(c -> c.getStatistics() != null)) {
            if (preparationId == null) {
                try (Stream<DataSetRow> records = getDataSetContentAsRows(id, dataSetMetadata.getRowMetadata())) {
                    analyzerService.analyzeFull(records, dataSetMetadata.getRowMetadata().getColumns());
                }
            } else {
                dataSetMetadata.setRowMetadata(getPreparationMetadata(preparationId));
            }
        }

        dataset.setMetadata(dataSetMetadata);
        dataset.setRecords(getDataSetContentAsRows(id, dataSetMetadata.getRowMetadata()));
        return dataset;
    }

    private RowMetadata getPreparationMetadata(String preparationId) {
        final PreparationDetailsGet preparationDetailsGet = context.getBean(PreparationDetailsGet.class, preparationId, Step.ROOT_STEP.id());
        try (InputStream details = preparationDetailsGet.execute()) {
            PreparationMessage preparationMessage = mapper.readerFor(PreparationMessage.class).readValue(details);
            return preparationMessage.getRowMetadata();
        } catch (Exception e) {
            throw new TDPException(UNABLE_TO_READ_PREPARATION, e, build().put("id", preparationId));
        }
    }

}
