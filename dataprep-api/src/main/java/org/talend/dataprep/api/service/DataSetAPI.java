package org.talend.dataprep.api.service;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.service.command.dataset.*;
import org.talend.dataprep.api.service.command.transformation.SuggestDataSetActions;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.metrics.Timed;

import com.netflix.hystrix.HystrixCommand;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@RestController
@Api(value = "api", basePath = "/api", description = "Data Preparation API")
public class DataSetAPI extends APIService {

    /**
     * Create a dataset from request body content.
     *
     * @param name The dataset name.
     * @param contentType the request content type used to distinguish dataset creation or import.
     * @param dataSetContent the dataset content from the http request body.
     * @return The dataset id.
     */
    @RequestMapping(value = "/api/datasets", method = POST, consumes = ALL_VALUE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a data set", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE, notes = "Create a new data set based on content provided in POST body. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too. Returns the id of the newly created data set.")
    public String create(
            @ApiParam(value = "User readable name of the data set (e.g. 'Finance Report 2015', 'Test Data Set').") @RequestParam(defaultValue = "", required = false) String name,
            @RequestHeader("Content-Type") String contentType,
            @ApiParam(value = "content") InputStream dataSetContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating dataset (pool: {} )...", getConnectionManager().getTotalStats());
        }
        HttpClient client = getClient();
        HystrixCommand<String> creation = getCommand(CreateDataSet.class, client, name, contentType, dataSetContent);
        String result = creation.execute();
        LOG.debug("Dataset creation done.");
        return result;
    }

    @RequestMapping(value = "/api/datasets/{id}", method = PUT, consumes = ALL_VALUE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Update a data set by id.", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE, //
    notes = "Create or update a data set based on content provided in PUT body with given id. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too. Returns the id of the newly created data set.")
    public String createOrUpdateById(
            @ApiParam(value = "User readable name of the data set (e.g. 'Finance Report 2015', 'Test Data Set').") @RequestParam(defaultValue = "", required = false) String name,
            @ApiParam(value = "Id of the data set to update / create") @PathVariable(value = "id") String id,
            @ApiParam(value = "content") InputStream dataSetContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating or updating dataset #{} (pool: {})...", id, getConnectionManager().getTotalStats());
        }
        HttpClient client = getClient();
        HystrixCommand<String> creation = getCommand(CreateOrUpdateDataSet.class, client, id, name, dataSetContent);
        String result = creation.execute();
        LOG.debug("Dataset creation or update for #{} done.", id);
        return result;
    }

    @RequestMapping(value = "/api/datasets/{id}", method = POST, consumes = ALL_VALUE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Update a dataset.", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE, //
    notes = "Update a data set based on content provided in POST body with given id. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too.")
    public String update(@ApiParam(value = "Id of the data set to update / create") @PathVariable(value = "id") String id,
            @ApiParam(value = "content") InputStream dataSetContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating or updating dataset #{} (pool: {})...", id, getConnectionManager().getTotalStats());
        }
        HttpClient client = getClient();
        HystrixCommand<String> creation = getCommand(UpdateDataSet.class, client, id, dataSetContent);
        String result = creation.execute();
        LOG.debug("Dataset creation or update for #{} done.", id);
        return result;
    }

    @RequestMapping(value = "/api/datasets/{datasetId}/column/{columnId}", method = POST, consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update a dataset.", consumes = APPLICATION_JSON_VALUE, //
        notes = "Update a data set based on content provided in POST body with given id. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too.")
    public void updateColumn(@PathVariable(value = "datasetId") @ApiParam(value = "Id of the dataset to update") final String datasetId,
                             @PathVariable(value = "columnId") @ApiParam(value = "Id of the column to update") final String columnId,
                             @ApiParam(value = "content") final InputStream body) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating or updating dataset #{} (pool: {})...", datasetId, getConnectionManager().getTotalStats());
        }

        final HttpClient client = getClient();
        final HystrixCommand<Void> creation = getCommand( UpdateColumn.class, client, datasetId, columnId, body );
        creation.execute();

        LOG.debug("Dataset creation or update for #{} done.", datasetId);
    }

    @RequestMapping(value = "/api/datasets/{id}", method = GET, consumes = ALL_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set by id.", produces = APPLICATION_JSON_VALUE, notes = "Get a data set based on given id.")
    public void get(
            @ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id,
            @RequestParam(defaultValue = "true") @ApiParam(name = "metadata", value = "Include metadata information in the response") boolean metadata,
            @RequestParam(defaultValue = "true") @ApiParam(name = "columns", value = "Include columns metadata information in the response") boolean columns,
            @RequestParam(required = false, defaultValue = "full") @ApiParam(name = "sample", value = "Size of the wanted sample, if missing or 'full', the full dataset is returned") String sample, //
            HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting dataset #{} (pool: {})...", id, getConnectionManager().getTotalStats());
        }
        response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
        HttpClient client = getClient();

        Long sampleValue;
        try {
            sampleValue = Long.parseLong(sample);
        } catch (NumberFormatException e) {
            sampleValue = null;
        }
        
        HystrixCommand<InputStream> retrievalCommand = getCommand(DataSetGet.class, client, id, metadata, columns, sampleValue);
        try (InputStream content = retrievalCommand.execute()){
            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(content, outputStream);
            outputStream.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request dataset #{} (pool: {}) done.", id, getConnectionManager().getTotalStats());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Clone a dataset from the given id
     *
     * @param id the dataset id to clone
     * @param name The dataset name.
     * @return The dataset id.
     */
    @RequestMapping(value = "/api/datasets/clone/{id}", method = GET, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a data set", produces = TEXT_PLAIN_VALUE, notes = "Clone a data set based the id provided.")
    public String cloneDataset(
        @ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id,
                               @ApiParam(value = "User readable name of the data set (e.g. 'Finance Report 2015', 'Test Data Set') if none the current name concat with ' Copy' will be used. Returns the id of the newly created data set.")
                               @RequestParam(defaultValue = "", required = false) String name) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Cloning dataset (pool: {} )...", getConnectionManager().getTotalStats());
        }

        HttpClient client = getClient();
        HystrixCommand<String> creation = getCommand(CloneDataSet.class, client, id, name);
        String result = creation.execute();
        LOG.debug("Dataset creation done.");
        return result;
    }

    @RequestMapping(value = "/api/datasets/preview/{id}", method = GET, consumes = ALL_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set by id.", produces = APPLICATION_JSON_VALUE, notes = "Get a data set based on given id.")
    public void preview(
            @ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id,
            @RequestParam(defaultValue = "true") @ApiParam(name = "metadata", value = "Include metadata information in the response") boolean metadata,
            @RequestParam(defaultValue = "true") @ApiParam(name = "columns", value = "Include columns metadata information in the response") boolean columns,
            @RequestParam(defaultValue = "") @ApiParam(name = "sheetName", value = "Sheet name to preview") String sheetName,
            HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting dataset #{} (pool: {})...", id, getConnectionManager().getTotalStats());
        }
        response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
        HttpClient client = getClient();
        HystrixCommand<InputStream> retrievalCommand = getCommand(DataSetPreview.class, client, id, metadata, columns, sheetName);
        try (InputStream content = retrievalCommand.execute()) {
            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(content, outputStream);
            outputStream.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request dataset #{} (pool: {}) done.", id, getConnectionManager().getTotalStats());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @RequestMapping(value = "/api/datasets", method = GET, consumes = ALL_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List data sets.", produces = APPLICATION_JSON_VALUE, notes = "Returns a list of data sets the user can use.")
    public void list(@ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "DATE", required = false) String sort,
                     @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "DESC", required = false) String order,
                     HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing datasets (pool: {})...", getConnectionManager().getTotalStats());
        }
        response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
        HttpClient client = getClient();
        HystrixCommand<InputStream> listCommand = getCommand(DataSetList.class, client, sort, order);
        try (InputStream content = listCommand.execute()) {
            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(content, outputStream);
            outputStream.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Listing datasets (pool: {}) done.", getConnectionManager().getTotalStats());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @RequestMapping(value = "/api/datasets/{id}", method = DELETE, consumes = ALL_VALUE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Delete a data set by id", notes = "Delete a data set content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content.")
    @Timed
    public void delete(@PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to delete") String dataSetId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delete dataset #{} (pool: {})...", dataSetId, getConnectionManager().getTotalStats());
        }
        HttpClient client = getClient();
        HystrixCommand<Void> deleteCommand = getCommand(DataSetDelete.class, client, dataSetId);

        deleteCommand.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing datasets (pool: {}) done.", getConnectionManager().getTotalStats());
        }
    }

    @RequestMapping(value = "/api/datasets/{id}/processcertification", method = PUT, consumes = ALL_VALUE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Ask certification for a dataset", notes = "Advance certification step of this dataset.")
    @Timed
    public void processCertification(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to update") String dataSetId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Ask certification for dataset #{}", dataSetId);
        }
        HttpClient client = getClient();
        HystrixCommand<Void> command = getCommand(DatasetCertification.class, client, dataSetId);
        command.execute();
    }

    @RequestMapping(value = "/api/datasets/{id}/actions", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get suggested actions for a whole data set.", notes = "Returns the suggested actions for the given dataset in decreasing order of likeness.")
    @Timed
    public void suggestDatasetActions(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Data set id to get suggestions from.") String dataSetId,
            HttpServletResponse response) {
        // Get dataset metadata
        HttpClient client = getClient();
        HystrixCommand<DataSetMetadata> retrieveMetadata = getCommand(DataSetGetMetadata.class, client, dataSetId);
        // Asks transformation service for suggested actions for column type and domain
        HystrixCommand<InputStream> getSuggestedActions = getCommand(SuggestDataSetActions.class, client, retrieveMetadata);
        // Returns actions
        try (InputStream content = getSuggestedActions.execute()) {
            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(content, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @RequestMapping(value = "/api/datasets/favorite/{id}", method = POST, consumes = ALL_VALUE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Set or Unset the dataset as favorite for the current user.", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE, //
    notes = "Specify if a dataset is or is not a favorite for the current user.")
    public String favorite(
            @ApiParam(value = "Id of the favorite data set ") @PathVariable(value = "id") String id,
            @RequestParam(defaultValue = "false") @ApiParam(name = "unset", value = "When true, will remove the dataset from favorites, if false (default) this will set the dataset as favorite.") boolean unset) {
        if (LOG.isDebugEnabled()) {
            LOG.debug((unset ? "Unset" : "Set") + " favorite dataset #{} (pool: {})...", id, getConnectionManager()
                    .getTotalStats());
        }
        HttpClient client = getClient();
        HystrixCommand<String> creation = getCommand(SetFavorite.class, client, id, unset);
        String result = creation.execute();
        LOG.debug("Set Favorite for user (can'tget user now) #{} done.", id);
        return result;
    }

}
