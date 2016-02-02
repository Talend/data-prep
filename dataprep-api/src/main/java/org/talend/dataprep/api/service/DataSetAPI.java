//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetMoveRequest;
import org.talend.dataprep.api.service.command.common.HttpResponse;
import org.talend.dataprep.api.service.command.dataset.*;
import org.talend.dataprep.api.service.command.transformation.SuggestDataSetActions;
import org.talend.dataprep.api.service.command.transformation.SuggestLookupActions;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.metrics.Timed;

import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
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
            @ApiParam(value = "The folder path to create the entry.") @RequestParam(defaultValue = "/", required = false) String folderPath,
            @RequestHeader("Content-Type") String contentType,
            @ApiParam(value = "content") InputStream dataSetContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating dataset (pool: {} )...", getConnectionStats());
        }
        HttpClient client = getClient();
        HystrixCommand<String> creation = getCommand(CreateDataSet.class, client, name, contentType, dataSetContent, folderPath);
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
            LOG.debug("Creating or updating dataset #{} (pool: {})...", id, getConnectionStats());
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
            LOG.debug("Creating or updating dataset #{} (pool: {})...", id, getConnectionStats());
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
            LOG.debug("Creating or updating dataset #{} (pool: {})...", datasetId, getConnectionStats());
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
            @RequestParam(required = false, defaultValue = "full") @ApiParam(name = "sample", value = "Size of the wanted sample, if missing or 'full', the full dataset is returned") String sample, //
            final OutputStream output) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting dataset #{} (pool: {})...", id, getConnectionStats());
        }
        HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
        HttpClient client = getClient();

        Long sampleValue;
        try {
            sampleValue = Long.parseLong(sample);
        } catch (NumberFormatException e) {
            sampleValue = null;
        }
        
        HystrixCommand<InputStream> retrievalCommand = getCommand(DataSetGet.class, client, id, metadata, sampleValue);
        try (InputStream content = retrievalCommand.execute()){
            IOUtils.copyLarge(content, output);
            output.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request dataset #{} (pool: {}) done.", id, getConnectionStats());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Return the dataset metadata.
     *
     * @param id the wanted dataset metadata.
     * @return the dataset metadata or no content if not found.
     */
    @RequestMapping(value = "/api/datasets/{id}/metadata", method = GET, consumes = ALL_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set metadata by id.", produces = APPLICATION_JSON_VALUE, notes = "Get a data set metadata based on given id.")
    public DataSetMetadata getMetadata(@ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting dataset metadata #{} (pool: {})...", id, getConnectionStats());
        }

        HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
        HttpClient client = getClient();

        HystrixCommand<DataSetMetadata> getMetadataCommand = getCommand(DataSetGetMetadata.class, client, id);
        final DataSetMetadata metadata = getMetadataCommand.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Request dataset metadata #{} (pool: {}) done.", id, getConnectionStats());
        }
        return metadata;
    }

    /**
     * Clone a dataset from the given id
     *
     * @param id the dataset id to clone
     * @param folderPath the folder path to clone the dataset
     * @param cloneName the name of the dataset clone
     */
    @RequestMapping(value = "/api/datasets/clone/{id}", method = PUT, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a data set", produces = TEXT_PLAIN_VALUE, notes = "Clone a data set based the id provided.")
    public void cloneDataset(
        @ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id,
        @ApiParam(value = "The name of the cloned dataset.") @RequestParam(defaultValue = "", required = false) String cloneName,
        @ApiParam(value = "The folder path to create the entry.") @RequestParam(defaultValue = "", required = false) String folderPath,
        final OutputStream output) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cloning dataset (pool: {} )...", getConnectionStats());
        }
        HttpClient client = getClient();
        HystrixCommand<HttpResponse> creation = getCommand(CloneDataSet.class, client, id, folderPath, cloneName);
        HttpResponse result = creation.execute();
        LOG.debug("Dataset clone done.");
        try {
            HttpResponseContext.status(HttpStatus.valueOf(result.getStatusCode()));
            HttpResponseContext.header("Content-Type", result.getContentType());
            IOUtils.write(result.getHttpContent(), output);
            output.flush();
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Move a data set to an other folder.
     *
     * @param dataSetId the dataset id to move.
     * @param dataSetMoveRequest the move request.
     */
    @RequestMapping(value = "/api/datasets/move/{id}", method = PUT, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Clone a data set", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.ALL_VALUE, notes = "Move a data set to an other folder.")
    @Timed
    public void move(@PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to clone") String dataSetId,
            @ApiParam(value = "the parameters to move the dataset.") @RequestBody(required = true) DataSetMoveRequest dataSetMoveRequest,
            final OutputStream output) throws IOException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Moving dataset (pool: {} )...", getConnectionStats());
        }
        HttpClient client = getClient();
        HystrixCommand<HttpResponse> creation = getCommand(MoveDataSet.class, client, //
                dataSetId, //
                dataSetMoveRequest.getFolderPath(), //
                dataSetMoveRequest.getNewFolderPath(), //
                dataSetMoveRequest.getNewName());
        HttpResponse result = creation.execute();
        LOG.debug("Dataset move done.");

        HttpResponseContext.header("Content-Type", result.getContentType());
        HttpResponseContext.status(HttpStatus.valueOf(result.getStatusCode()));
        if (result.getStatusCode() != HttpStatus.OK.value()) {
            try {
                IOUtils.write(result.getHttpContent(), output);
                output.flush();
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        }
    }

    @RequestMapping(value = "/api/datasets/preview/{id}", method = GET, consumes = ALL_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set by id.", produces = APPLICATION_JSON_VALUE, notes = "Get a data set based on given id.")
    public void preview(@ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id,
            @RequestParam(defaultValue = "true") @ApiParam(name = "metadata", value = "Include metadata information in the response") boolean metadata,
            @RequestParam(defaultValue = "") @ApiParam(name = "sheetName", value = "Sheet name to preview") String sheetName,
            final OutputStream output) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting dataset #{} (pool: {})...", id, getConnectionStats());
        }
        HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
        HttpClient client = getClient();
        HystrixCommand<InputStream> retrievalCommand = getCommand(DataSetPreview.class, client, id, metadata, sheetName);
        try (InputStream content = retrievalCommand.execute()) {
            IOUtils.copyLarge(content, output);
            output.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request dataset #{} (pool: {}) done.", id, getConnectionStats());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @RequestMapping(value = "/api/datasets", method = GET, consumes = ALL_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List data sets.", produces = APPLICATION_JSON_VALUE, notes = "Returns a list of data sets the user can use.")
    public void list(@ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "DATE", required = false) String sort,
                     @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "DESC", required = false) String order,
                     final OutputStream output) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing datasets (pool: {})...", getConnectionStats());
        }
        HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
        HttpClient client = getClient();
        HystrixCommand<InputStream> listCommand = getCommand(DataSetList.class, client, sort, order);
        try (InputStream content = listCommand.execute()) {
            IOUtils.copyLarge(content, output);
            output.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Listing datasets (pool: {}) done.", getConnectionStats());
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
            LOG.debug("Delete dataset #{} (pool: {})...", dataSetId, getConnectionStats());
        }
        HttpClient client = getClient();
        HystrixCommand<Void> deleteCommand = getCommand(DataSetDelete.class, client, dataSetId);

        deleteCommand.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing datasets (pool: {}) done.", getConnectionStats());
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
            final OutputStream output) {
        // Get dataset metadata
        HttpClient client = getClient();
        HystrixCommand<DataSetMetadata> retrieveMetadata = getCommand(DataSetGetMetadata.class, client, dataSetId);
        // Asks transformation service for suggested actions for column type and domain...
        HystrixCommand<String> getSuggestedActions = getCommand(SuggestDataSetActions.class, client, retrieveMetadata);
        // ... also adds lookup actions
        HystrixCommand<InputStream> getLookupActions = getCommand(SuggestLookupActions.class, client, getSuggestedActions,
                dataSetId);
        // Returns actions
        try (InputStream content = getLookupActions.execute()) {
            IOUtils.copyLarge(content, output);
            output.flush();
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
            LOG.debug((unset ? "Unset" : "Set") + " favorite dataset #{} (pool: {})...", id, getConnectionStats());
        }
        HttpClient client = getClient();
        HystrixCommand<String> creation = getCommand(SetFavorite.class, client, id, unset);
        String result = creation.execute();
        LOG.debug("Set Favorite for user (can'tget user now) #{} done.", id);
        return result;
    }

    @RequestMapping(value = "/api/datasets/encodings", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List supported dataset encodings.", notes = "Returns the supported dataset encodings.")
    @Timed
    public void listEncodings(final OutputStream output) {

        // Get dataset metadata
        HystrixCommand<InputStream> retrieveEncodings = getCommand(DataSetGetEncodings.class, getClient());

        try (InputStream content = retrieveEncodings.execute()) {
            IOUtils.copyLarge(content, output);
            output.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Listing datasets (pool: {}) done.", getConnectionStats());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
