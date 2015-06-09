package org.talend.dataprep.dataset.service;

import static org.talend.dataprep.api.dataset.DataSetMetadata.Builder.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.jms.Message;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.DistributedLock;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetGovernance.Certification;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.dataset.exception.DataSetErrorCodes;
import org.talend.dataprep.dataset.service.analysis.AsynchronousDataSetAnalyzer;
import org.talend.dataprep.dataset.service.analysis.SynchronousDataSetAnalyzer;
import org.talend.dataprep.dataset.store.DataSetContentStore;
import org.talend.dataprep.dataset.store.DataSetMetadataRepository;
import org.talend.dataprep.exception.CommonErrorCodes;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.TDPExceptionContext;
import org.talend.dataprep.exception.json.JsonErrorCodeDescription;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;
import org.talend.dataprep.schema.DraftValidator;
import org.talend.dataprep.schema.FormatGuess;
import org.talend.dataprep.schema.SchemaParserResult;

import com.wordnik.swagger.annotations.*;

@RestController
@Api(value = "datasets", basePath = "/datasets", description = "Operations on data sets")
public class DataSetService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-YYYY HH:mm"); //$NON-NLS-1

    private static final Logger LOG = LoggerFactory.getLogger(DataSetService.class);

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
    }

    @Autowired
    AsynchronousDataSetAnalyzer[] asynchronousAnalyzers;

    @Autowired
    List<SynchronousDataSetAnalyzer> synchronousAnalyzers;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    private DataSetMetadataRepository dataSetMetadataRepository;

    @Autowired
    private DataSetContentStore contentStore;

    @Autowired
    private FormatGuess.Factory formatGuessFactory;

    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @PostConstruct
    public void initialize() {
        synchronousAnalyzers.sort((analyzer1, analyzer2) -> analyzer1.order() - analyzer2.order());
    }

    private void queueEvents(String id) {
        // Calls all synchronous analysis first
        for (SynchronousDataSetAnalyzer synchronousDataSetAnalyzer : synchronousAnalyzers) {
            synchronousDataSetAnalyzer.analyze(id);
        }
        // Then use JMS queue for all optional analysis
        for (AsynchronousDataSetAnalyzer asynchronousDataSetAnalyzer : asynchronousAnalyzers) {
            jmsTemplate.send(asynchronousDataSetAnalyzer.destination(), session -> {
                Message message = session.createMessage();
                message.setStringProperty("dataset.id", id); //$NON-NLS-1
                return message;
            });
        }
    }

    /**
     * @return Get user name from Spring Security context, return "anonymous" if no user is currently logged in.
     */
    private static String getUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String author;
        if (principal != null) {
            author = principal.toString();
        } else {
            author = "anonymous"; //$NON-NLS-1
        }
        return author;
    }

    @RequestMapping(value = "/datasets", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List all data sets", notes = "Returns the list of data sets the current user is allowed to see. Creation date is always displayed in UTC time zone.")
    @Timed
    public Iterable<DataSetMetadata> list() {
        final Spliterator<DataSetMetadata> iterator = dataSetMetadataRepository.list().spliterator();
        final Stream<DataSetMetadata> stream = StreamSupport.stream(iterator, false);
        return stream.filter(metadata -> !metadata.getLifecycle().importing()).collect(Collectors.toList());
    }

    /**
     * Creates a new data set and returns the new data set id as text in the response.
     *
     * @param name An optional name for the new data set (might be <code>null</code>).
     * @param dataSetContent The raw content of the data set (might be a CSV, XLS...).
     * @param response The HTTP response to interact with caller.
     * @return The new data id.
     * @see #get(boolean, boolean, String, HttpServletResponse)
     */
    @RequestMapping(value = "/datasets", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a data set", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE, notes = "Create a new data set based on content provided in POST body. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too. Returns the id of the newly created data set.")
    @Timed
    @VolumeMetered
    public String create(
            @ApiParam(value = "User readable name of the data set (e.g. 'Finance Report 2015', 'Test Data Set').") @RequestParam(defaultValue = "", required = false) String name,
            @ApiParam(value = "content") InputStream dataSetContent, HttpServletResponse response) {
        response.setHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE); //$NON-NLS-1$
        final String id = UUID.randomUUID().toString();
        DataSetMetadata dataSetMetadata = metadata().id(id).name(name).author(getUserName()).created(System.currentTimeMillis())
                .build();
        dataSetMetadata.getLifecycle().importing(true); // Indicate data set is being imported
        // Save data set content
        contentStore.storeAsRaw(dataSetMetadata, dataSetContent);
        // Create the new data set
        dataSetMetadataRepository.add(dataSetMetadata);
        // Queue events (format analysis, content indexing for search...)
        queueEvents(id);
        return id;
    }

    /**
     * Returns the data set content for given id. Service might return {@link HttpServletResponse#SC_ACCEPTED} if the
     * data set exists but analysis is not yet fully completed so content is not yet ready to be served.
     *
     * @param metadata If <code>true</code>, includes data set metadata information.
     * @param columns If <code>true</code>, includes column metadata information (column types...).
     * @param dataSetId A data set id.
     * @param response The HTTP response to interact with caller.
     */
    @RequestMapping(value = "/datasets/{id}/content", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set by id", notes = "Get a data set content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content.")
    @Timed
    @ResponseBody
    public DataSet get(
            @RequestParam(defaultValue = "true") @ApiParam(name = "metadata", value = "Include metadata information in the response") boolean metadata, //
            @RequestParam(defaultValue = "true") @ApiParam(name = "columns", value = "Include column information in the response") boolean columns, //
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the requested data set") String dataSetId, //
            HttpServletResponse response) {
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE); //$NON-NLS-1$
        final DistributedLock lock = dataSetMetadataRepository.createDatasetMetadataLock(dataSetId);
        lock.lock();
        try {
            DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
            if (dataSetMetadata == null) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return DataSet.empty(); // No data set, returns empty content.
            }
            if (dataSetMetadata.getLifecycle().importing()) {
                // Data set is being imported, this is an error since user should not have an id to a being-created
                // data set (create() operation is a blocking operation).
                final TDPExceptionContext context = TDPExceptionContext.build().put("id", dataSetId); //$NON-NLS-1$
                throw new TDPException(DataSetErrorCodes.UNABLE_TO_SERVE_DATASET_CONTENT, context);
            }
            // Build the result
            DataSet dataSet = new DataSet();
            if (metadata) {
                dataSet.setMetadata(dataSetMetadata);
            }
            if (columns) {
                dataSet.setColumns(dataSetMetadata.getRow().getColumns());
            }
            dataSet.setRecords(contentStore.stream(dataSetMetadata));
            return dataSet;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Deletes a data set with provided id.
     *
     * @param dataSetId A data set id. If data set id is unknown, no exception nor status code to indicate this is set.
     */
    @RequestMapping(value = "/datasets/{id}", method = RequestMethod.DELETE, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Delete a data set by id", notes = "Delete a data set content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content.")
    @Timed
    public void delete(@PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to delete") String dataSetId) {
        DataSetMetadata metadata = dataSetMetadataRepository.get(dataSetId);
        final DistributedLock lock = dataSetMetadataRepository.createDatasetMetadataLock(dataSetId);
        try {
            lock.lock();
            if (metadata != null) {
                contentStore.delete(metadata);
                dataSetMetadataRepository.remove(dataSetId);
            }
        } finally {
            lock.unlock();
        }
    }

    @RequestMapping(value = "/datasets/{id}/processcertification", method = RequestMethod.PUT, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Ask certification for a dataset", notes = "Advance certification step of this dataset.")
    @Timed
    public void processCertification(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to update") String dataSetId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Ask certification for dataset #{}", dataSetId);
        }

        DistributedLock datasetLock = dataSetMetadataRepository.createDatasetMetadataLock(dataSetId);
        datasetLock.lock();
        try {
            DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
            LOG.trace("Current certification step is " + dataSetMetadata.getGovernance().getCertificationStep());

            if (dataSetMetadata.getGovernance().getCertificationStep() == Certification.NONE) {
                dataSetMetadata.getGovernance().setCertificationStep(Certification.PENDING);
                dataSetMetadataRepository.add(dataSetMetadata);
            } else if (dataSetMetadata.getGovernance().getCertificationStep() == Certification.PENDING) {
                dataSetMetadata.getGovernance().setCertificationStep(Certification.CERTIFIED);
                dataSetMetadataRepository.add(dataSetMetadata);
            } else if (dataSetMetadata.getGovernance().getCertificationStep() == Certification.CERTIFIED) {
                dataSetMetadata.getGovernance().setCertificationStep(Certification.NONE);
                dataSetMetadataRepository.add(dataSetMetadata);
            }

            LOG.debug("New certification step is " + dataSetMetadata.getGovernance().getCertificationStep());
        } finally {
            datasetLock.unlock();
        }
    }

    /**
     * Updates a data set content and metadata. If no data set exists for given id, data set is silently created.
     *
     * @param dataSetId The id of data set to be updated.
     * @param name The new name for the data set.
     * @param dataSetContent The new content for the data set. If empty, existing content will <b>not</b> be replaced.
     * For delete operation, look at {@link #delete(String)}.
     */
    @RequestMapping(value = "/datasets/{id}/raw", method = RequestMethod.PUT, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Update a data set by id", consumes = "text/plain", notes = "Update a data set content based on provided id and PUT body. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too.")
    @Timed
    @VolumeMetered
    public void updateRawDataSet(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to update") String dataSetId, //
            @RequestParam(value = "name", required = false) @ApiParam(name = "name", value = "New value for the data set name") String name, //
            @ApiParam(value = "content") InputStream dataSetContent) {
        final DistributedLock lock = dataSetMetadataRepository.createDatasetMetadataLock(dataSetId);
        try {
            lock.lock();
            DataSetMetadata.Builder datasetBuilder = metadata().id(dataSetId);
            if (name != null) {
                datasetBuilder = datasetBuilder.name(name);
            }
            DataSetMetadata dataSetMetadata = datasetBuilder.build();
            // Save data set content
            contentStore.storeAsRaw(dataSetMetadata, dataSetContent);
            dataSetMetadataRepository.add(dataSetMetadata);
        } finally {
            lock.unlock();
        }
        // Content was changed, so queue events (format analysis, content indexing for search...)
        queueEvents(dataSetId);
    }

    /**
     * Returns the data set {@link DataSetMetadata metadata} for given <code>dataSetId</code>.
     *
     * @param dataSetId A data set id. If <code>null</code> <b>or</b> if no data set with provided id exits, operation
     * returns {@link HttpServletResponse#SC_NO_CONTENT}
     * @param response The HTTP response to interact with caller.
     */
    @RequestMapping(value = "/datasets/{id}/metadata", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get metadata information of a data set by id", notes = "Get metadata information of a data set by id. Not valid or non existing data set id returns empty content.")
    @ApiResponses({ @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = "Data set does not exist."),
            @ApiResponse(code = HttpServletResponse.SC_ACCEPTED, message = "Data set metadata is not yet ready.") })
    @Timed
    @ResponseBody
    public DataSet getMetadata(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set metadata") String dataSetId, //
            HttpServletResponse response) {
        if (dataSetId == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return null;
        }
        DataSetMetadata metadata = dataSetMetadataRepository.get(dataSetId);
        if (metadata == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return null;
        }
        if (!metadata.getLifecycle().schemaAnalyzed()) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            return DataSet.empty();
        }
        DataSet dataSet = new DataSet();
        dataSet.setMetadata(metadata);
        dataSet.setColumns(metadata.getRow().getColumns());
        return dataSet;
    }

    /**
     * List all dataset related error codes.
     */
    @RequestMapping(value = "/datasets/errors", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all dataset related error codes.", notes = "Returns the list of all dataset related error codes.")
    @Timed
    public void listErrors(HttpServletResponse response) {
        try {
            // need to cast the typed dataset errors into mock ones to use json parsing
            List<JsonErrorCodeDescription> errors = new ArrayList<>(DataSetErrorCodes.values().length);
            for (DataSetErrorCodes code : DataSetErrorCodes.values()) {
                errors.add(new JsonErrorCodeDescription(code));
            }
            builder.build().writer().writeValue(response.getOutputStream(), errors);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Returns preview of the the data set content for given id (first 100 rows). Service might return
     * {@link HttpServletResponse#SC_ACCEPTED} if the data set exists but analysis is not yet fully completed so content
     * is not yet ready to be served.
     *
     * @param metadata If <code>true</code>, includes data set metadata information.
     * @param columns If <code>true</code>, includes column metadata information (column types...).
     * @param sheetName the sheet name to preview
     * @param dataSetId A data set id.
     * @param response The HTTP response to interact with caller.
     */
    @RequestMapping(value = "/datasets/{id}/preview", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data preview set by id", notes = "Get a data set preview content based on provided id. Not valid or non existing data set id returns empty content. Data set not in drat status will return a redirect 301")
    @Timed
    @ResponseBody
    public DataSet preview(
            @RequestParam(defaultValue = "true") @ApiParam(name = "metadata", value = "Include metadata information in the response") boolean metadata, //
            @RequestParam(defaultValue = "true") @ApiParam(name = "columns", value = "Include column information in the response") boolean columns, //
            @RequestParam(defaultValue = "") @ApiParam(name = "sheetName", value = "Sheet name to preview") String sheetName, //
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the requested data set") String dataSetId, //
            HttpServletResponse response) {
        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        if (dataSetMetadata == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return DataSet.empty(); // No data set, returns empty content.
        }
        if (!dataSetMetadata.isDraft()) {
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            return DataSet.empty(); // dataset not anymore a draft so preview doesn't make sense.
        }
        if (StringUtils.isNotEmpty(sheetName)) {
            dataSetMetadata.setSheetName(sheetName);
        }
        // take care of previous datas without schema parser result
        if (dataSetMetadata.getSchemaParserResult() != null) {
            // sheet not yet set correctly so use the first one
            if (StringUtils.isEmpty(dataSetMetadata.getSheetName())) {
                String theSheetName = dataSetMetadata.getSchemaParserResult().getSheetContents().get(0).getName();
                LOG.debug("preview for dataSetMetadata: {} with sheetName: {}", dataSetId, theSheetName);
                dataSetMetadata.setSheetName(theSheetName);
            }

            String theSheetName = dataSetMetadata.getSheetName();

            Optional<SchemaParserResult.SheetContent> sheetContentFound = dataSetMetadata.getSchemaParserResult()
                    .getSheetContents().stream().filter(sheetContent -> theSheetName.equals(sheetContent.getName())).findFirst();

            if (!sheetContentFound.isPresent()) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return DataSet.empty(); // No sheet found, returns empty content.
            }

            List<ColumnMetadata> columnMetadatas = sheetContentFound.get().getColumnMetadatas();

            if (dataSetMetadata.getRow() == null) {
                dataSetMetadata.setRowMetadata(new RowMetadata(Collections.emptyList()));
            }

            dataSetMetadata.getRow().setColumns(columnMetadatas);
        } else {
            LOG.warn("dataset#{} has draft status but any SchemaParserResult");
        }
        // Build the result
        DataSet dataSet = new DataSet();
        if (metadata) {
            dataSet.setMetadata(dataSetMetadata);
        }
        if (columns) {
            dataSet.setColumns(dataSetMetadata.getRow().getColumns());
        }
        dataSet.setRecords(contentStore.stream(dataSetMetadata).limit(100));
        return dataSet;
    }

    /**
     * Updates a data set content and metadata. If no data set exists for given id, data set is silently created.
     *
     * @param dataSetId The id of data set to be updated.
     * @param dataSet The new content for the data set. If empty, existing content will <b>not</b> be replaced.
     * For delete operation, look at {@link #delete(String)}.
     */
    @RequestMapping(value = "/datasets/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update a data set by id", consumes = "text/plain", notes = "Update a data set metadata according to the content of the PUT body. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content.")
    @Timed
    @VolumeMetered
    public void updateDataSet(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to update") String dataSetId,
            @RequestBody DataSet dataSet) {
        final DistributedLock lock = dataSetMetadataRepository.createDatasetMetadataLock(dataSetId);
        lock.lock();
        try {
            final DataSetMetadata dataSetMetadata = dataSet.getMetadata();
            LOG.debug("updateDataSet: {}", dataSetMetadata);
            // we retry information we do not update
            DataSetMetadata previous = dataSetMetadataRepository.get(dataSetId);
            previous.setName(dataSetMetadata.getName());

            Optional<SchemaParserResult.SheetContent> sheetContentFound = previous.getSchemaParserResult().getSheetContents()
                    .stream().filter(sheetContent -> dataSetMetadata.getSheetName().equals(sheetContent.getName())).findFirst();

            if (sheetContentFound.isPresent()) {
                List<ColumnMetadata> columnMetadatas = sheetContentFound.get().getColumnMetadatas();
                if (previous.getRow() == null) {
                    previous.setRowMetadata(new RowMetadata(Collections.emptyList()));
                }
                previous.getRow().setColumns(columnMetadatas);
            }
            // Set the user-selected sheet name
            previous.setSheetName(dataSetMetadata.getSheetName());
            previous.setSchemaParserResult(null);
            FormatGuess formatGuess = formatGuessFactory.getFormatGuess(dataSetMetadata.getContent().getFormatGuessId());
            DraftValidator draftValidator = formatGuess.getDraftValidator();
            // Validate that the new data set metadata removes the draft status
            DraftValidator.Result result = draftValidator.validate(dataSetMetadata);
            if (result.draft) {
                // FIXME what to do here?? exception?
                LOG.warn("dataSetMetadata#{} still a draft", dataSetId);
                return;
            }
            // Data set metadata to update is no longer a draft
            previous.setDraft(false);
            dataSetMetadataRepository.add(previous); // Save it
            // all good mate!! so send that to jms
            // Asks for a in depth schema analysis (for column type information).
            queueEvents(dataSetId);
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        } finally {
            lock.unlock();
        }
    }

}
