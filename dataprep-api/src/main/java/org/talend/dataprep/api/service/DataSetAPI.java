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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.command.dataset.*;
import org.talend.dataprep.api.service.command.preparation.PreparationList;
import org.talend.dataprep.api.service.command.transformation.SuggestDataSetActions;
import org.talend.dataprep.api.service.command.transformation.SuggestLookupActions;
import org.talend.dataprep.command.dataset.DataSetGet;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.PublicAPI;

import com.fasterxml.jackson.core.type.TypeReference;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;

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
            @RequestHeader("Content-Type") String contentType, @ApiParam(value = "content") InputStream dataSetContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating dataset (pool: {} )...", getConnectionStats());
        }
        HystrixCommand<String> creation = getCommand(CreateDataSet.class, name, contentType, dataSetContent);
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
        HystrixCommand<String> creation = getCommand(CreateOrUpdateDataSet.class, id, name, dataSetContent);
        String result = creation.execute();
        LOG.debug("Dataset creation or update for #{} done.", id);
        return result;
    }

    @RequestMapping(value = "/api/datasets/{id}/copy", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Copy the dataset.", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE,
            notes = "Copy the dataset, returns the id of the copied created data set.")
    public String copy(
            @ApiParam(value = "Name of the copy") @RequestParam(required = false) String name,
            @ApiParam(value = "Id of the data set to update / create") @PathVariable(value = "id") String id) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Copying {} (pool: {})...", id, getConnectionStats());
        }

        HystrixCommand<String> creation = getCommand(CopyDataSet.class, id, name);
        String result = creation.execute();
        LOG.info("Dataset {} copied --> {} named '{}'", id, result, name);
        return result;
    }

    @RequestMapping(value = "/api/datasets/{id}/metadata", method = PUT, consumes = ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update a data set metadata by id.", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, //
            notes = "Update a data set metadata based on content provided in PUT body with given id. For documentation purposes. Returns the id of the updated data set metadata.")
    public void updateMetadata(
            @ApiParam(value = "Id of the data set metadata to be updated") @PathVariable(value = "id") String id,
            @ApiParam(value = "content") InputStream dataSetContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating or updating dataset #{} (pool: {})...", id, getConnectionStats());
        }
        HystrixCommand<String> creation = getCommand(UpdateDataSet.class,id, dataSetContent);
        creation.execute();
        LOG.debug("Dataset creation or update for #{} done.", id);
    }

    @RequestMapping(value = "/api/datasets/{id}", method = POST, consumes = ALL_VALUE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Update a dataset.", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE, //
    notes = "Update a data set based on content provided in POST body with given id. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too.")
    public String update(@ApiParam(value = "Id of the data set to update / create") @PathVariable(value = "id") String id,
            @ApiParam(value = "content") InputStream dataSetContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating or updating dataset #{} (pool: {})...", id, getConnectionStats());
        }
        HystrixCommand<String> creation = getCommand(UpdateDataSet.class, id, dataSetContent);
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

        final HystrixCommand<Void> creation = getCommand( UpdateColumn.class, datasetId, columnId, body );
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

        Long sampleValue;
        try {
            sampleValue = Long.parseLong(sample);
        } catch (NumberFormatException e) {
            sampleValue = null;
        }
        
        HystrixCommand<InputStream> retrievalCommand = getCommand(DataSetGet.class, id, metadata, sampleValue);
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

        HystrixCommand<DataSetMetadata> getMetadataCommand = getCommand(DataSetGetMetadata.class, id);
        final DataSetMetadata metadata = getMetadataCommand.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Request dataset metadata #{} (pool: {}) done.", id, getConnectionStats());
        }
        return metadata;
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
        HystrixCommand<InputStream> retrievalCommand = getCommand(DataSetPreview.class, id, metadata, sheetName);
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
        HystrixCommand<InputStream> listCommand = getCommand(DataSetList.class, sort, order);
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

    /**
     * Returns a list containing all data sets metadata that are compatible with the data set with id <tt>id</tt>. If no
     * compatible data set is found an empty list is returned. The data set with id <tt>dataSetId</tt> is never returned
     * in the list.
     * 
     * @param id the specified data set id
     * @param sort the sort criterion: either name or date.
     * @param order the sorting order: either asc or desc
     * @return a list containing all data sets metadata that are compatible with the data set with id <tt>id</tt> and
     * empty list if no data set is compatible.
     */
    @RequestMapping(value = "/api/datasets/{id}/compatibledatasets", method = GET, consumes = ALL_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List compatible data sets.", produces = APPLICATION_JSON_VALUE, notes = "Returns a list of data sets that are compatible with the specified one.")
    public void listCompatibleDatasets(@ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id,
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "DATE", required = false) String sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "DESC", required = false) String order,
            final OutputStream output) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing compatible datasets (pool: {})...", getConnectionStats());
        }
        HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
        HystrixCommand<InputStream> listCommand = getCommand(CompatibleDataSetList.class, id, sort, order);
        try (InputStream content = listCommand.execute()) {
            IOUtils.copyLarge(content, output);
            output.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Listing compatible datasets (pool: {}) done.", getConnectionStats());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Returns a list containing all preparations that are compatible with the data set with id <tt>id</tt>. If no
     * compatible preparation is found an empty list is returned.
     *
     * @param dataSetId the specified data set id
     * @param sort the sort criterion: either name or date.
     * @param order the sorting order: either asc or desc
     * @return a list containing all preparations that are compatible with the data set with id <tt>id</tt> and empty
     * list if no preparation is compatible.
     */
    @RequestMapping(value = "/api/datasets/{id}/compatiblepreparations", method = GET, consumes = ALL_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List compatible preparations.", produces = APPLICATION_JSON_VALUE, notes = "Returns a list of data sets that are compatible with the specified one.")
    public void listCompatiblePreparations(
            @ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String dataSetId,
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "MODIF", required = false) String sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "DESC", required = false) String order,
            final OutputStream output) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing compatible preparations (pool: {})...", getConnectionStats());
        }
        HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
        try {
            // get the list of compatible data sets
            final ByteArrayOutputStream temp = new ByteArrayOutputStream();
            listCompatibleDatasets(dataSetId, "", order, temp);
            final Iterable<DataSetMetadata> dataSetMetadataCollection = readServiceResult(
                    new TypeReference<Iterable<DataSetMetadata>>() {
                    }, temp);
            final Set<String> compatibleDataSetIds = StreamSupport.stream(dataSetMetadataCollection.spliterator(), false)
                    .map(DataSetMetadata::getId).collect(Collectors.toSet());
            // add the current dataset
            compatibleDataSetIds.add(dataSetId);

            // get list of preparations
            HystrixCommand<InputStream> listCommand = getCommand(PreparationList.class, PreparationList.Format.LONG, sort,
                    order);
            try {
                String preparationsJson = IOUtils.toString(listCommand.execute());
                final Collection<Preparation> preparationsList = mapper.readerFor(new TypeReference<Collection<Preparation>>() {
                }).readValue(preparationsJson);

                // filter and keep only data sets ids that are compatible
                List<Preparation> preparations = preparationsList.stream()
                        .filter(p -> compatibleDataSetIds.contains(p.getDataSetId())).collect(Collectors.toList());

                InputStream content = IOUtils.toInputStream(mapper.writeValueAsString(preparations));
                IOUtils.copyLarge(content, output);
                output.flush();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Listing compatible datasets (pool: {}) done.", getConnectionStats());
                }
            } catch (HystrixRuntimeException e) {
                throw new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_PREPARATION_LIST, e);
            }

        } catch (IOException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_COMPATIBLE_PREPARATIONS, e);
        }

    }

    @RequestMapping(value = "/api/datasets/{id}", method = DELETE, consumes = ALL_VALUE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Delete a data set by id", notes = "Delete a data set content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content.")
    @Timed
    public void delete(@PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to delete") String dataSetId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delete dataset #{} (pool: {})...", dataSetId, getConnectionStats());
        }
        HystrixCommand<Void> deleteCommand = getCommand(DataSetDelete.class, dataSetId);

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
        HystrixCommand<Void> command = getCommand(DatasetCertification.class, dataSetId);
        command.execute();
    }

    @RequestMapping(value = "/api/datasets/{id}/actions", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get suggested actions for a whole data set.", notes = "Returns the suggested actions for the given dataset in decreasing order of likeness.")
    @Timed
    public void suggestDatasetActions(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Data set id to get suggestions from.") String dataSetId,
            final OutputStream output) {
        // Get dataset metadata
        HystrixCommand<DataSetMetadata> retrieveMetadata = getCommand(DataSetGetMetadata.class, dataSetId);
        // Asks transformation service for suggested actions for column type and domain...
        HystrixCommand<String> getSuggestedActions = getCommand(SuggestDataSetActions.class, retrieveMetadata);
        // ... also adds lookup actions
        HystrixCommand<InputStream> getLookupActions = getCommand(SuggestLookupActions.class, getSuggestedActions,
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
        HystrixCommand<String> creation = getCommand(SetFavorite.class, id, unset);
        String result = creation.execute();
        LOG.debug("Set Favorite for user (can'tget user now) #{} done.", id);
        return result;
    }

    private <T> T readServiceResult(TypeReference<T> type, ByteArrayOutputStream byteArray) {
        final String json;
        try {
            json = new String(byteArray.toByteArray());
            return mapper.readerFor(type).readValue(json);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        } finally {
            IOUtils.closeQuietly(byteArray);
        }

    }

    @RequestMapping(value = "/api/datasets/encodings", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List supported dataset encodings.", notes = "Returns the supported dataset encodings.")
    @Timed
    @PublicAPI
    public void listEncodings(final OutputStream output) {

        // Get dataset metadata
        HystrixCommand<InputStream> retrieveEncodings = getCommand(DataSetGetEncodings.class);

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

    @RequestMapping(value = "/api/datasets/imports", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List supported imports for a dataset.", notes = "Returns the supported import types.")
    @Timed
    @PublicAPI
    public void listImports(final OutputStream output) {

        // Get dataset metadata
        HystrixCommand<InputStream> retrieveImports = getCommand(DataSetGetImports.class);

        try (InputStream content = retrieveImports.execute()) {
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
