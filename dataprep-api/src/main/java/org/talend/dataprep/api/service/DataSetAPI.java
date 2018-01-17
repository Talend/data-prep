// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.talend.dataprep.command.CommandHelper.*;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.Import;
import org.talend.dataprep.api.dataset.statistics.SemanticDomain;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.api.EnrichedDataSetMetadata;
import org.talend.dataprep.api.service.command.dataset.*;
import org.talend.dataprep.api.service.command.preparation.PreparationList;
import org.talend.dataprep.api.service.command.preparation.PreparationSearchByDataSetId;
import org.talend.dataprep.api.service.command.transformation.SuggestDataSetActions;
import org.talend.dataprep.api.service.command.transformation.SuggestLookupActions;
import org.talend.dataprep.command.CommandHelper;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.command.dataset.DataSetGet;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.dataset.service.UserDataSetMetadata;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.PublicAPI;
import org.talend.dataprep.util.SortAndOrderHelper;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;

import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class DataSetAPI extends APIService {

    /**
     * Create a dataset from request body content.
     *
     * @param name           The dataset name.
     * @param contentType    the request content type used to distinguish dataset creation or import.
     * @param dataSetContent the dataset content from the http request body.
     * @return The dataset id.
     */
    @RequestMapping(value = "/api/datasets", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a data set", produces = TEXT_PLAIN_VALUE, notes = "Create a new data set based on content provided in POST body. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too. Returns the id of the newly created data set.")
    @Timed
    public Callable<String> create(
            @ApiParam(value = "User readable name of the data set (e.g. 'Finance Report 2015', 'Test Data Set').") @RequestParam(defaultValue = "", required = false) String name,
            @ApiParam(value = "An optional tag to be added in data set metadata once created.") @RequestParam(defaultValue = "", required = false) String tag,
            @ApiParam(value = "Size of the data set, in bytes.") @RequestParam(defaultValue = "0") long size,
            @RequestHeader(CONTENT_TYPE) String contentType, @ApiParam(value = "content") InputStream dataSetContent) {
        return () -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating dataset (pool: {} )...", getConnectionStats());
            }
            try {
                HystrixCommand<String> creation = getCommand(CreateDataSet.class, name, tag, contentType, size, dataSetContent);
                return creation.execute();
            } finally {
                LOG.debug("Dataset creation done.");
            }
        };
    }

    @RequestMapping(value = "/api/datasets/{id}", method = PUT, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Update a data set by id.", produces = TEXT_PLAIN_VALUE, //
            notes = "Create or update a data set based on content provided in PUT body with given id. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too. Returns the id of the newly created data set.")
    @Timed
    public Callable<String> createOrUpdateById(
            @ApiParam(value = "User readable name of the data set (e.g. 'Finance Report 2015', 'Test Data Set').") @RequestParam(defaultValue = "", required = false) String name,
            @ApiParam(value = "Id of the data set to update / create") @PathVariable(value = "id") String id,
            @ApiParam(value = "Size of the data set, in bytes.") @RequestParam(defaultValue = "0") long size,
            @ApiParam(value = "content") InputStream dataSetContent) {
        return () -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating or updating dataset #{} (pool: {})...", id, getConnectionStats());
            }
            HystrixCommand<String> creation = getCommand(CreateOrUpdateDataSet.class, id, name, size, dataSetContent);
            String result = creation.execute();
            LOG.debug("Dataset creation or update for #{} done.", id);
            return result;
        };
    }

    @RequestMapping(value = "/api/datasets/{id}/copy", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Copy the dataset.", produces = TEXT_PLAIN_VALUE,
            notes = "Copy the dataset, returns the id of the copied created data set.")
    @Timed
    public Callable<String> copy(
            @ApiParam(value = "Name of the copy") @RequestParam(required = false) String name,
            @ApiParam(value = "Id of the data set to update / create") @PathVariable(value = "id") String id) {
        return () -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Copying {} (pool: {})...", id, getConnectionStats());
            }

            HystrixCommand<String> creation = getCommand(CopyDataSet.class, id, name);
            String result = creation.execute();
            LOG.info("Dataset {} copied --> {} named '{}'", id, result, name);
            return result;
        };
    }

    @RequestMapping(value = "/api/datasets/{id}/metadata", method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update a data set metadata by id.", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, //
            notes = "Update a data set metadata based on content provided in PUT body with given id. For documentation purposes. Returns the id of the updated data set metadata.")
    @Timed
    public void updateMetadata(
            @ApiParam(value = "Id of the data set metadata to be updated") @PathVariable(value = "id") String id,
            @ApiParam(value = "content") InputStream dataSetContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating or updating dataset #{} (pool: {})...", id, getConnectionStats());
        }
        HystrixCommand<String> updateDataSetCommand = getCommand(UpdateDataSet.class, id, dataSetContent);
        updateDataSetCommand.execute();
        LOG.debug("Dataset creation or update for #{} done.", id);
    }

    @RequestMapping(value = "/api/datasets/{id}", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Update a dataset.", produces = TEXT_PLAIN_VALUE, //
            notes = "Update a data set based on content provided in POST body with given id. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too.")
    @Timed
    public Callable<String> update(@ApiParam(value = "Id of the data set to update / create") @PathVariable(value = "id") String id,
                         @ApiParam(value = "content") InputStream dataSetContent) {
        return () -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating or updating dataset #{} (pool: {})...", id, getConnectionStats());
            }
            HystrixCommand<String> creation = getCommand(UpdateDataSet.class, id, dataSetContent);
            String result = creation.execute();
            LOG.debug("Dataset creation or update for #{} done.", id);
            return result;
        };
    }

    @RequestMapping(value = "/api/datasets/{datasetId}/column/{columnId}", method = POST, consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update a dataset.", consumes = APPLICATION_JSON_VALUE, //
            notes = "Update a data set based on content provided in POST body with given id. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too.")
    @Timed
    public void updateColumn(@PathVariable(value = "datasetId") @ApiParam(value = "Id of the dataset to update") final String datasetId,
                             @PathVariable(value = "columnId") @ApiParam(value = "Id of the column to update") final String columnId,
                             @ApiParam(value = "content") final InputStream body) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating or updating dataset #{} (pool: {})...", datasetId, getConnectionStats());
        }

        final HystrixCommand<Void> creation = getCommand(UpdateColumn.class, datasetId, columnId, body);
        creation.execute();

        LOG.debug("Dataset creation or update for #{} done.", datasetId);
    }

    @RequestMapping(value = "/api/datasets/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set by id.", produces = APPLICATION_JSON_VALUE, notes = "Get a data set based on given id.")
    @Timed
    public StreamingResponseBody get(@ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id,
            @ApiParam(value = "Whether output should be the full data set (true) or not (false).") @RequestParam(value = "fullContent", defaultValue = "false") boolean fullContent,
            @ApiParam(value = "Filter for retrieved content.") @RequestParam(value = "filter", defaultValue = "") String filter,
            @ApiParam(value = "Whether to include internal technical properties (true) or not (false).") @RequestParam(value = "includeTechnicalProperties", defaultValue = "false") boolean includeTechnicalProperties) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting dataset #{} (pool: {})...", id, getConnectionStats());
        }
        try {
            final HystrixCommand<InputStream> retrievalCommand = getCommand(DataSetGet.class, id, fullContent, includeTechnicalProperties, filter);
            HttpResponseContext.contentType(APPLICATION_JSON_VALUE);
            return toStreaming(retrievalCommand);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request dataset #{} (pool: {}) done.", id, getConnectionStats());
            }
        }
    }

    /**
     * Return the dataset metadata.
     *
     * @param id the wanted dataset metadata.
     * @return the dataset metadata or no content if not found.
     */
    @RequestMapping(value = "/api/datasets/{id}/metadata", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set metadata by id.", produces = APPLICATION_JSON_VALUE, notes = "Get a data set metadata based on given id.")
    @Timed
    public DataSetMetadata getMetadata(@ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting dataset metadata #{} (pool: {})...", id, getConnectionStats());
        }
        try {
            final HystrixCommand<DataSetMetadata> getMetadataCommand = getCommand(DataSetGetMetadata.class, id);
            return getMetadataCommand.execute();
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request dataset metadata #{} (pool: {}) done.", id, getConnectionStats());
            }
        }
    }


    @RequestMapping(value = "/api/datasets/preview/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set by id.", produces = APPLICATION_JSON_VALUE, notes = "Get a data set based on given id.")
    @Timed
    public ResponseEntity<StreamingResponseBody> preview(@ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id,
                                                         @RequestParam(defaultValue = "true") @ApiParam(name = "metadata", value = "Include metadata information in the response") boolean metadata,
                                                         @RequestParam(defaultValue = "") @ApiParam(name = "sheetName", value = "Sheet name to preview") String sheetName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting dataset #{} (pool: {})...", id, getConnectionStats());
        }
        try {
            GenericCommand<InputStream> retrievalCommand = getCommand(DataSetPreview.class, id, metadata, sheetName);
            return toStreaming(retrievalCommand);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request dataset #{} (pool: {}) done.", id, getConnectionStats());
            }
        }
    }

    @RequestMapping(value = "/api/datasets", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List data sets.", produces = APPLICATION_JSON_VALUE, notes = "Returns a list of data sets the user can use.")
    @Timed
    public Callable<Stream<UserDataSetMetadata>> list(
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "creationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "desc") Order order,
            @ApiParam(value = "Filter on name containing the specified name") @RequestParam(defaultValue = "") String name,
            @ApiParam(value = "Filter on certified data sets") @RequestParam(defaultValue = "false") boolean certified,
            @ApiParam(value = "Filter on favorite data sets") @RequestParam(defaultValue = "false") boolean favorite,
            @ApiParam(value = "Filter on recent data sets") @RequestParam(defaultValue = "false") boolean limit) {
        return () -> {
            try {
                GenericCommand<InputStream> listCommand = getCommand(DataSetList.class, sort, order, name, certified, favorite, limit);
                return toStream(UserDataSetMetadata.class, mapper, listCommand);
            } finally {
                LOG.info("listing datasets done [favorite: {}, certified: {}, name: {}, limit: {}]", favorite, certified, name,
                        limit);
            }
        };
    }

    @RequestMapping(value = "/api/datasets/summary", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List data sets summary.", produces = APPLICATION_JSON_VALUE, notes = "Returns a list of data sets summary the user can use.")
    @Timed
    public Callable<Stream<EnrichedDataSetMetadata>> listSummary(
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "creationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "desc") Order order,
            @ApiParam(value = "Filter on name containing the specified name") @RequestParam(defaultValue = "") String name,
            @ApiParam(value = "Filter on certified data sets") @RequestParam(defaultValue = "false") boolean certified,
            @ApiParam(value = "Filter on favorite data sets") @RequestParam(defaultValue = "false") boolean favorite,
            @ApiParam(value = "Filter on recent data sets") @RequestParam(defaultValue = "false") boolean limit) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing datasets summary (pool: {})...", getConnectionStats());
        }
        return () -> {
            GenericCommand<InputStream> listDataSets = getCommand(DataSetList.class, sort, order, name, certified, favorite, limit);
            return Flux.from(CommandHelper.toPublisher(UserDataSetMetadata.class, mapper, listDataSets)) //
                    .map(m -> {
                        LOG.debug("found dataset {} in the summary list" + m.getName());
                        // Add the related preparations list to the given dataset metadata.
                        final PreparationSearchByDataSetId getPreparations = getCommand(PreparationSearchByDataSetId.class,
                                m.getId());
                        return Flux.from(CommandHelper.toPublisher(Preparation.class, mapper, getPreparations)).collectList() //
                                .map(preparations -> {
                                    final List<Preparation> list = preparations.stream() //
                                            .filter(p -> p.getSteps() != null) //
                                            .collect(Collectors.toList());
                                    return new EnrichedDataSetMetadata(m, list);
                                }) //
                                .block();
                    }) //
                    .toStream(1);
        };
    }

    /**
     * Returns a list containing all data sets metadata that are compatible with the data set with id <tt>id</tt>. If no
     * compatible data set is found an empty list is returned. The data set with id <tt>dataSetId</tt> is never returned
     * in the list.
     *
     * @param id    the specified data set id
     * @param sort  the sort criterion: either name or date.
     * @param order the sorting order: either asc or desc
     * @return a list containing all data sets metadata that are compatible with the data set with id <tt>id</tt> and
     * empty list if no data set is compatible.
     */
    @RequestMapping(value = "/api/datasets/{id}/compatibledatasets", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List compatible data sets.", produces = APPLICATION_JSON_VALUE, notes = "Returns a list of data sets that are compatible with the specified one.")
    @Timed
    public Callable<Stream<UserDataSetMetadata>> listCompatibleDatasets(
            @ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id,
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "creationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "desc") Order order) {
        return () -> toStream(UserDataSetMetadata.class, mapper, getCommand(CompatibleDataSetList.class, id, sort, order));
    }

    /**
     * Returns a list containing all preparations that are compatible with the data set with id <tt>id</tt>. If no
     * compatible preparation is found an empty list is returned.
     *
     * @param dataSetId the specified data set id
     * @param sort      the sort criterion: either name or date.
     * @param order     the sorting order: either asc or desc
     */
    @RequestMapping(value = "/api/datasets/{id}/compatiblepreparations", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List compatible preparations.", produces = APPLICATION_JSON_VALUE, notes = "Returns a list of data sets that are compatible with the specified one.")
    @Timed
    public Callable<Stream<Preparation>> listCompatiblePreparations(
            @ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String dataSetId,
            @ApiParam(value = "Sort key (by name or date), defaults to 'modification'.") @RequestParam(defaultValue = "lastModificationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "desc") Order order) {
        return () -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Listing compatible preparations (pool: {})...", getConnectionStats());
            }
            // get the list of compatible data sets
            GenericCommand<InputStream> compatibleDataSetList = getCommand(CompatibleDataSetList.class, dataSetId, sort, order);
            final Mono<List<DataSetMetadata>> compatibleList = Flux
                    .from(toPublisher(DataSetMetadata.class, mapper, compatibleDataSetList)) //
                    .collectList() //
                    .cache(); // Keep it in cache for later reuse
            // get list of preparations
            GenericCommand<InputStream> preparationList = getCommand(PreparationList.class, SortAndOrderHelper.Format.LONG, sort, order);
            return Flux.from(toPublisher(Preparation.class, mapper, preparationList)) //
                    .filter(p -> compatibleList.flatMapIterable(l -> l) //
                            .map(DataSetMetadata::getId) //
                            .any(id -> StringUtils.equals(id, p.getDataSetId()) || dataSetId.equals(p.getDataSetId())) //
                            .block() //
                    ) //
                    .toStream(1);
        };
    }

    @RequestMapping(value = "/api/datasets/{id}", method = DELETE)
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

    @RequestMapping(value = "/api/datasets/{id}/actions", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get suggested actions for a whole data set.", notes = "Returns the suggested actions for the given dataset in decreasing order of likeness.")
    @Timed
    public StreamingResponseBody suggestDatasetActions(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Data set id to get suggestions from.") String dataSetId) {
        // Get dataset metadata
        HystrixCommand<DataSetMetadata> retrieveMetadata = getCommand(DataSetGetMetadata.class, dataSetId);
        // Asks transformation service for suggested actions for column type and domain...
        HystrixCommand<String> getSuggestedActions = getCommand(SuggestDataSetActions.class, retrieveMetadata);
        // ... also adds lookup actions
        HystrixCommand<InputStream> getLookupActions = getCommand(SuggestLookupActions.class, getSuggestedActions,
                dataSetId);
        // Returns actions
        return toStreaming(getLookupActions);
    }

    @RequestMapping(value = "/api/datasets/favorite/{id}", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Set or Unset the dataset as favorite for the current user.", produces = TEXT_PLAIN_VALUE, //
            notes = "Specify if a dataset is or is not a favorite for the current user.")
    @Timed
    public Callable<String> favorite(
            @ApiParam(value = "Id of the favorite data set ") @PathVariable(value = "id") String id,
            @RequestParam(defaultValue = "false") @ApiParam(name = "unset", value = "When true, will remove the dataset from favorites, if false (default) this will set the dataset as favorite.") boolean unset) {
        return () -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug((unset ? "Unset" : "Set") + " favorite dataset #{} (pool: {})...", id, getConnectionStats());
            }
            HystrixCommand<String> creation = getCommand(SetFavorite.class, id, unset);
            String result = creation.execute();
            LOG.debug("Set Favorite for user (can'tget user now) #{} done.", id);
            return result;
        };
    }

    @RequestMapping(value = "/api/datasets/encodings", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List supported dataset encodings.", notes = "Returns the supported dataset encodings.")
    @Timed
    @PublicAPI
    public Callable<Stream<String>> listEncodings() {
        return () -> CommandHelper.toStream(String.class, mapper, getCommand(DataSetGetEncodings.class));
    }

    @RequestMapping(value = "/api/datasets/imports/{import}/parameters", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Fetch the parameters needed to imports a dataset.", notes = "Returns the parameters needed to imports a dataset.")
    @Timed
    @PublicAPI
    public ResponseEntity<StreamingResponseBody> getImportParameters(@PathVariable("import") final String importType) {
        return toStreaming(getCommand(DataSetGetImportParameters.class, importType));
    }

    @RequestMapping(value = "/api/datasets/imports", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List supported imports for a dataset.", notes = "Returns the supported import types.")
    @Timed
    @PublicAPI
    public Callable<Stream<Import>> listImports() {
        return () -> CommandHelper.toStream(Import.class, mapper, getCommand(DataSetGetImports.class));
    }

    /**
     * Return the semantic types for a given dataset / column.
     *
     * @param datasetId the dataset id.
     * @param columnId the column id.
     * @return the semantic types for a given dataset / column.
     */
    @RequestMapping(value = "/api/datasets/{datasetId}/columns/{columnId}/types", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "list the types of the wanted column", notes = "This list can be used by user to change the column type.")
    @Timed
    @PublicAPI
    public Callable<Stream<SemanticDomain>> getDataSetColumnSemanticCategories(
            @ApiParam(value = "The dataset id") @PathVariable String datasetId,
            @ApiParam(value = "The column id") @PathVariable String columnId) {
        return () -> {
            LOG.debug("listing semantic types for dataset {}, column {}", datasetId, columnId);
            return CommandHelper.toStream(SemanticDomain.class, mapper, getCommand(GetDataSetColumnTypes.class, datasetId, columnId));
        };
    }
}
