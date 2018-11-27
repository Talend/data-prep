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

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.talend.dataprep.command.CommandHelper.toStream;
import static org.talend.dataprep.command.CommandHelper.toStreaming;
import static org.talend.dataprep.dataset.adapter.Dataset.CertificationState.CERTIFIED;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.action.ActionForm;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DatasetDTO;
import org.talend.dataprep.api.dataset.DatasetDetailsDTO;
import org.talend.dataprep.api.dataset.statistics.SemanticDomain;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.service.command.dataset.CompatibleDataSetList;
import org.talend.dataprep.api.service.command.dataset.CopyDataSet;
import org.talend.dataprep.api.service.command.dataset.CreateDataSet;
import org.talend.dataprep.api.service.command.dataset.CreateOrUpdateDataSet;
import org.talend.dataprep.api.service.command.dataset.DataSetDelete;
import org.talend.dataprep.api.service.command.dataset.DataSetGetImportParameters;
import org.talend.dataprep.api.service.command.dataset.DataSetPreview;
import org.talend.dataprep.api.service.command.dataset.GetDataSetColumnTypes;
import org.talend.dataprep.api.service.command.dataset.SetFavorite;
import org.talend.dataprep.api.service.command.dataset.UpdateColumn;
import org.talend.dataprep.api.service.command.dataset.UpdateDataSet;
import org.talend.dataprep.api.service.command.preparation.PreparationList;
import org.talend.dataprep.api.service.command.preparation.PreparationSearchByDataSetId;
import org.talend.dataprep.api.service.command.transformation.SuggestLookupActions;
import org.talend.dataprep.command.CommandHelper;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.configuration.EncodingSupport;
import org.talend.dataprep.dataset.adapter.Dataset.CertificationState;
import org.talend.dataprep.dataset.adapter.DatasetClient;
import org.talend.dataprep.metrics.LogTimed;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.PublicAPI;
import org.talend.dataprep.util.SortAndOrderHelper;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;
import org.talend.dataprep.util.StringsHelper;

import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
public class DataSetAPI extends APIService {

    @Autowired
    private DatasetClient datasetClient;

    @Value("${dataset.list.limit:10}")
    private int datasetListLimit;

    /**
     * Create a dataset from request body content.
     *
     * @param name The dataset name.
     * @param contentType the request content type used to distinguish dataset creation or import.
     * @param dataSetContent the dataset content from the http request body.
     * @return The dataset id.
     */
    @RequestMapping(value = "/api/datasets", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a data set", produces = TEXT_PLAIN_VALUE,
            notes = "Create a new data set based on content provided in POST body. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too. Returns the id of the newly created data set.")
    @Timed
    public Callable<String> create(
            @ApiParam(
                    value = "User readable name of the data set (e.g. 'Finance Report 2015', 'Test Data Set').") @RequestParam(
                            defaultValue = "", required = false) final String name,
            @ApiParam(value = "An optional tag to be added in data set metadata once created.") @RequestParam(
                    defaultValue = "", required = false) String tag,
            @ApiParam(value = "Size of the data set, in bytes.") @RequestParam(defaultValue = "0") long size,
            @RequestHeader(CONTENT_TYPE) String contentType, @ApiParam(value = "content") InputStream dataSetContent) {
        return () -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating dataset (pool: {} )...", getConnectionStats());
            }

            try {
                HystrixCommand<String> creation = getCommand(CreateDataSet.class, StringsHelper.normalizeString(name),
                        tag, contentType, size, dataSetContent);
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
            @ApiParam(
                    value = "User readable name of the data set (e.g. 'Finance Report 2015', 'Test Data Set').") @RequestParam(
                            defaultValue = "", required = false) String name,
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
    public Callable<String> copy(@ApiParam(value = "Name of the copy") @RequestParam(required = false) String name,
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
    @ApiOperation(value = "Update a data set metadata by id.", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE, //
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

    @RequestMapping(value = "/api/datasets/{datasetId}/column/{columnId}", method = POST,
            consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update a dataset.", consumes = APPLICATION_JSON_VALUE, //
            notes = "Update a data set based on content provided in POST body with given id. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too.")
    @Timed
    public void updateColumn(
            @PathVariable(value = "datasetId") @ApiParam(value = "Id of the dataset to update") final String datasetId,
            @PathVariable(value = "columnId") @ApiParam(value = "Id of the column to update") final String columnId,
            @ApiParam(value = "content") final InputStream body) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating or updating dataset #{} (pool: {})...", datasetId, getConnectionStats());
        }

        final HystrixCommand<Void> creation = getCommand(UpdateColumn.class, datasetId, columnId, body);
        creation.execute();

        LOG.debug("Dataset creation or update for #{} done.", datasetId);
    }

    @GetMapping(value = "/api/datasets/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set by id.", produces = APPLICATION_JSON_VALUE,
            notes = "Get a data set based on given id.")
    @Timed
    @LogTimed
    public DataSet get(@ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id,
            @ApiParam(value = "Whether output should be the full data set (true) or not (false).") @RequestParam(
                    value = "fullContent", defaultValue = "false") boolean fullContent,
            @ApiParam(value = "Filter for retrieved content.") @RequestParam(value = "filter",
                    defaultValue = "") String filter,
            @ApiParam(value = "Whether to include internal technical properties (true) or not (false).") @RequestParam(
                    value = "includeTechnicalProperties", defaultValue = "false") boolean includeTechnicalProperties) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting dataset #{} (pool: {})...", id, getConnectionStats());
        }
        try {
            return datasetClient.getDataSet(id, fullContent, includeTechnicalProperties, filter);
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
    @ApiOperation(value = "Get a data set metadata by id.", produces = APPLICATION_JSON_VALUE,
            notes = "Get a data set metadata based on given id.")
    @Timed
    public DataSetMetadata
            getMetadata(@ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting dataset metadata #{} (pool: {})...", id, getConnectionStats());
        }
        try {
            return datasetClient.getDataSetMetadata(id);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request dataset metadata #{} (pool: {}) done.", id, getConnectionStats());
            }
        }
    }

    @RequestMapping(value = "/api/datasets/preview/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set by id.", produces = APPLICATION_JSON_VALUE,
            notes = "Get a data set based on given id.")
    @Timed
    public ResponseEntity<StreamingResponseBody> preview(
            @ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id,
            @RequestParam(defaultValue = "true") @ApiParam(name = "metadata",
                    value = "Include metadata information in the response") boolean metadata,
            @RequestParam(defaultValue = "") @ApiParam(name = "sheetName",
                    value = "Sheet name to preview") String sheetName) {
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
    @ApiOperation(value = "List data sets.", produces = APPLICATION_JSON_VALUE,
            notes = "Returns a list of data sets the user can use.")
    @Timed
    public Stream<DatasetDTO> list(
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(
                    defaultValue = "creationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(
                    defaultValue = "desc") Order order,
            @ApiParam(value = "Filter on name containing the specified name") @RequestParam(
                    defaultValue = "") String name,
            @ApiParam(value = "Filter on certified data sets") @RequestParam(defaultValue = "false") boolean certified,
            @ApiParam(value = "Filter on favorite data sets") @RequestParam(defaultValue = "false") boolean favorite,
            @ApiParam(value = "Filter on recent data sets") @RequestParam(defaultValue = "false") boolean limit) {
        try {
            CertificationState certification = certified ? CERTIFIED : null;
            Boolean filterOnFavorite = favorite ? Boolean.TRUE : null;
            Stream<DatasetDTO> datasetStream = datasetClient.listDataSetMetadata(certification, filterOnFavorite);

            if (isNotBlank(name)) {
                datasetStream = datasetStream.filter(ds -> containsIgnoreCase(ds.getName(), name));
            }

            if (certified) {
                datasetStream = datasetStream.filter(dataset -> dataset.getCertification() == CERTIFIED);
            }

            if (limit) {
                datasetStream = datasetStream.limit(datasetListLimit);
            }

            return datasetStream //
                    .sorted(SortAndOrderHelper.getDatasetDTOComparator(sort, order));
        } finally {
            LOG.info("listing datasets done [favorite: {}, certified: {}, name: {}, limit: {}]", favorite, certified,
                    name, limit);
        }
    }

    /**
     * Return the dataset details.
     *
     * @param id the wanted dataset details.
     * @return the dataset datails or no content if not found.
     */
    @RequestMapping(value = "/api/datasets/{id}/details", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a data set detail by id.", produces = APPLICATION_JSON_VALUE,
            notes = "Get a data set metadata based on given id.")
    @Timed
    public DatasetDetailsDTO
            getDetails(@ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting dataset details #{} (pool: {})...", id, getConnectionStats());
        }
        try {
            DatasetDetailsDTO details = datasetClient.getDataSetDetails(id);

            List<DatasetDetailsDTO.Preparation> preps = getPreparation(details.getId());

            details.setPreparations(preps);

            return details;

        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request dataset details #{} (pool: {}) done.", id, getConnectionStats());
            }
        }
    }

    /**
     * Return the list of preparation using a dataset
     *
     * @param id the wanted dataset.
     * @return the list of preparation using the dataset
     */
    @RequestMapping(value = "/api/datasets/{id}/preparations", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the list of preparation using a dataset by the dataset id.",
            produces = APPLICATION_JSON_VALUE, notes = "Get the list of preparation using a dataset by the dataset id.")
    @Timed
    public List<DatasetDetailsDTO.Preparation>
            getPreparation(@ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting preparations using dataset #{} (pool: {})...", id, getConnectionStats());
        }
        try {
            DatasetDetailsDTO details = datasetClient.getDataSetDetails(id);

            // Add the related preparations list to the given dataset metadata.
            final PreparationSearchByDataSetId getPreparations =
                    getCommand(PreparationSearchByDataSetId.class, details.getId());

            List<DatasetDetailsDTO.Preparation> preps = new ArrayList<>();

            toStream(PreparationDTO.class, mapper, getPreparations) //
                    .filter(p -> p.getSteps() != null)
                    .forEach(p -> preps.add(new DatasetDetailsDTO.Preparation(p.getId(), p.getName(),
                            (long) p.getSteps().size(), p.getLastModificationDate())));

            return preps;

        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Request preparations using dataset #{} (pool: {}) done.", id, getConnectionStats());
            }
        }
    }

    @RequestMapping(value = "/api/datasets/summary", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List data sets summary.", produces = APPLICATION_JSON_VALUE,
            notes = "Returns a list of data sets summary the user can use.")
    @Timed
    public Stream<DatasetDTO> listSummary(
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(
                    defaultValue = "creationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(
                    defaultValue = "desc") Order order,
            @ApiParam(value = "Filter on name containing the specified name") @RequestParam(
                    defaultValue = "") String name,
            @ApiParam(value = "Filter on certified data sets") @RequestParam(defaultValue = "false") boolean certified,
            @ApiParam(value = "Filter on favorite data sets") @RequestParam(defaultValue = "false") boolean favorite,
            @ApiParam(value = "Filter on recent data sets") @RequestParam(defaultValue = "false") boolean limit) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing datasets summary (pool: {})...", getConnectionStats());
        }
        return list(sort, order, name, certified, favorite, limit);

    }

    /**
     * Returns a list containing all preparations that are compatible with the data set with id <tt>id</tt>. If no
     * compatible preparation is found an empty list is returned.
     *
     * @param dataSetId the specified data set id
     * @param sort the sort criterion: either name or date.
     * @param order the sorting order: either asc or desc
     */
    @RequestMapping(value = "/api/datasets/{id}/compatiblepreparations", method = GET,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List compatible preparations.", produces = APPLICATION_JSON_VALUE,
            notes = "Returns a list of data sets that are compatible with the specified one.")
    @Timed
    public Stream<PreparationDTO> listCompatiblePreparations(
            @ApiParam(value = "Id of the data set to get") @PathVariable(value = "id") String dataSetId,
            @ApiParam(value = "Sort key (by name or date), defaults to 'modification'.") @RequestParam(
                    defaultValue = "lastModificationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(
                    defaultValue = "desc") Order order) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing compatible preparations (pool: {})...", getConnectionStats());
        }
        // get the list of compatible data sets
        GenericCommand<InputStream> compatibleDataSetList =
                getCommand(CompatibleDataSetList.class, dataSetId, sort, order);

        final List<String> compatibleList = toStream(DataSetMetadata.class, mapper, compatibleDataSetList)
                .map(DataSetMetadata::getId)
                .collect(Collectors.toList());

        // get list of preparations
        GenericCommand<InputStream> preparationList = getCommand(PreparationList.class, sort, order);

        return toStream(PreparationDTO.class, mapper, preparationList)
                .filter(p -> compatibleList.contains(p.getDataSetId()) || dataSetId.equals(p.getDataSetId()));
    }

    @RequestMapping(value = "/api/datasets/{id}", method = DELETE)
    @ApiOperation(value = "Delete a data set by id",
            notes = "Delete a data set content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content.")
    @Timed
    public ResponseEntity<String> delete(@PathVariable(value = "id") @ApiParam(name = "id",
            value = "Id of the data set to delete") String dataSetId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delete dataset #{} (pool: {})...", dataSetId, getConnectionStats());
        }
        HystrixCommand<ResponseEntity<String>> deleteCommand = getCommand(DataSetDelete.class, dataSetId);
        try {
            return deleteCommand.execute();
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Listing datasets (pool: {}) done.", getConnectionStats());
            }
        }
    }

    @RequestMapping(value = "/api/datasets/{id}/actions", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get suggested actions for a whole data set.",
            notes = "Returns the suggested actions for the given dataset in decreasing order of likeness.")
    @Timed
    public List<ActionForm> suggestDatasetActions(@PathVariable(value = "id") @ApiParam(name = "id",
            value = "Data set id to get suggestions from.") String dataSetId) {
        HystrixCommand<List<ActionForm>> getLookupActions = getCommand(SuggestLookupActions.class,
                new HystrixCommand<DataSetMetadata>(GenericCommand.DATASET_GROUP) {

                    @Override
                    protected DataSetMetadata run() {
                        return getMetadata(dataSetId);
                    }
                });

        return getLookupActions.execute();
    }

    @RequestMapping(value = "/api/datasets/favorite/{id}", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Set or Unset the dataset as favorite for the current user.", produces = TEXT_PLAIN_VALUE, //
            notes = "Specify if a dataset is or is not a favorite for the current user.")
    @Timed
    public Callable<String> favorite(
            @ApiParam(value = "Id of the favorite data set ") @PathVariable(value = "id") String id,
            @RequestParam(defaultValue = "false") @ApiParam(name = "unset",
                    value = "When true, will remove the dataset from favorites, if false (default) this will set the dataset as favorite.") boolean unset) {
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
    public Stream<String> listEncodings() {
        return EncodingSupport.getSupportedCharsets().stream().map(Charset::displayName);
    }

    @RequestMapping(value = "/api/datasets/imports/{import}/parameters", method = GET,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Fetch the parameters needed to imports a dataset.",
            notes = "Returns the parameters needed to imports a dataset.")
    @Timed
    @PublicAPI
    public ResponseEntity<StreamingResponseBody> getImportParameters(@PathVariable("import") final String importType) {
        return toStreaming(getCommand(DataSetGetImportParameters.class, importType));
    }

    /**
     * Return the semantic types for a given dataset / column.
     *
     * @param datasetId the dataset id.
     * @param columnId the column id.
     * @return the semantic types for a given dataset / column.
     */
    @RequestMapping(value = "/api/datasets/{datasetId}/columns/{columnId}/types", method = GET,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "list the types of the wanted column",
            notes = "This list can be used by user to change the column type.")
    @Timed
    @PublicAPI
    public Stream<SemanticDomain> getDataSetColumnSemanticCategories(
            @ApiParam(value = "The dataset id") @PathVariable String datasetId,
            @ApiParam(value = "The column id") @PathVariable String columnId) {
        LOG.debug("listing semantic types for dataset {}, column {}", datasetId, columnId);
        return CommandHelper.toStream(SemanticDomain.class, mapper,
                getCommand(GetDataSetColumnTypes.class, datasetId, columnId));
    }
}
