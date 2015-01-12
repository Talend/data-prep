package org.talend.dataprep.dataset.service;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.dataset.objects.ColumnMetadata;
import org.talend.dataprep.dataset.objects.DataSetMetadata;
import org.talend.dataprep.dataset.store.DataSetContentStore;
import org.talend.dataprep.dataset.store.DataSetMetadataRepository;

import javax.jms.Message;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.talend.dataprep.dataset.objects.DataSetMetadata.Builder.id;

@RestController
@Api(value = "datasets", basePath = "/datasets", description = "Operations on data sets")
public class DataSetService {

    private final JsonFactory         factory = new JsonFactory();

    @Autowired
    JmsTemplate                       jmsTemplate;

    @Autowired
    private DataSetMetadataRepository dataSetMetadataRepository;

    @Autowired
    private DataSetContentStore       contentStore;

    private static void queueEvents(String id, JmsTemplate template) {
        String[] destinations = { Destinations.SCHEMA_ANALYSIS, Destinations.CONTENT_ANALYSIS };
        for (String destination : destinations) {
            template.send(destination, session -> {
                Message message = session.createMessage();
                message.setStringProperty("dataset.id", id); //$NON-NLS-1
                    return message;
                });
        }
    }

    /**
     * Add elements to responce header to add CORS support.
     */
    private void addResponseHeader(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization");
    }

    @RequestMapping(value = "/datasets", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List all data sets", notes = "Returns the list of data sets the current user is allowed to see.")
    public void list(HttpServletResponse response) {
        addResponseHeader(response);

        Iterable<DataSetMetadata> dataSets = dataSetMetadataRepository.list();
        try (JsonGenerator generator = factory.createJsonGenerator(response.getOutputStream())) {
            generator.writeStartArray();
            for (DataSetMetadata dataSetMetadata : dataSets) {
                generator.writeString(dataSetMetadata.getId());
            }
            generator.writeEndArray();
            generator.flush();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected I/O exception during message output.", e);
        }
    }

    @RequestMapping(value = "/datasets", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a data set", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE, notes = "Create a new data set based on content provided in POST body. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too. Returns the id of the newly created data set.")
    public String create(@ApiParam(value = "content") InputStream dataSetContent) {
        final String id = UUID.randomUUID().toString();
        DataSetMetadata dataSetMetadata = id(id).build();
        // Save data set content
        contentStore.store(dataSetMetadata, dataSetContent);
        // Create the new data set
        dataSetMetadataRepository.add(dataSetMetadata);
        // Queue events (schema analysis, content indexing for search...)
        queueEvents(id, jmsTemplate);
        return id;
    }

    @RequestMapping(value = "/datasets/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set by id", notes = "Get a data set content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content.")
    public void get(@PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the requested data set") String dataSetId,
            HttpServletResponse response) {
        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        if (dataSetMetadata == null) {
            return; // No data set, returns empty content.
        }
        try (JsonGenerator generator = factory.createJsonGenerator(response.getOutputStream())) {
            generator.writeStartObject();
            {
                // Write columns
                generator.writeFieldName("columns"); //$NON-NLS-1
                generator.writeStartArray();
                for (ColumnMetadata column : dataSetMetadata.getRow().getColumns()) {
                    generator.writeStartObject();
                    {
                        // Column name
                        generator.writeStringField("id", column.getName()); //$NON-NLS-1
                        // Column quality
                        if (dataSetMetadata.getLifecycle().qualityAnalyzed()) {
                            generator.writeFieldName("quality"); //$NON-NLS-1
                            generator.writeStartObject();
                            {
                                generator.writeNumberField("empty", 0); //$NON-NLS-1
                                generator.writeNumberField("invalid", 0); //$NON-NLS-1
                                generator.writeNumberField("valid", 0); //$NON-NLS-1
                            }
                            generator.writeEndObject();
                        }
                        // Column type
                        String typeName = dataSetMetadata.getLifecycle().schemaAnalyzed() ? column.getType().getName() : "N/A"; //$NON-NLS-1
                        generator.writeStringField("type", typeName); //$NON-NLS-1
                    }
                    generator.writeEndObject();
                }
                generator.writeEndArray();
                // Records
                generator.writeFieldName("records");
                generator.flush(); // <- Important! Flush before dumping records!
                response.getOutputStream().write(':');
                // Put here content as provided by data set store
                try (InputStream content = contentStore.get(dataSetMetadata)) {
                    IOUtils.copy(content, response.getOutputStream());
                }
            }
            generator.writeEndObject();
            generator.flush();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected I/O exception during message output.", e);
        }
    }

    @RequestMapping(value = "/datasets/{id}", method = RequestMethod.DELETE, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Delete a data set by id", notes = "Delete a data set content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content.")
    public void delete(@PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to delete") String dataSetId) {
        DataSetMetadata metadata = dataSetMetadataRepository.get(dataSetId);
        if (metadata != null) {
            contentStore.delete(metadata);
            dataSetMetadataRepository.remove(dataSetId);
        }
    }

    @RequestMapping(value = "/datasets/{id}", method = RequestMethod.PUT, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Update a data set by id", consumes = "text/plain", notes = "Update a data set content based on provided id and PUT body. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too.")
    public void update(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to update") String dataSetId,
            @ApiParam(value = "content") InputStream dataSetContent) {
        dataSetMetadataRepository.add(id(dataSetId).build());
        // Content was changed, so queue events (schema analysis, content indexing for search...)
        queueEvents(dataSetId, jmsTemplate);
    }
}
