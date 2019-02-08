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

package org.talend.dataprep.dataset.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_CONTENT;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.INVALID_DATASET_NAME;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.MAX_STORAGE_MAY_BE_EXCEEDED;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.UNABLE_CREATE_DATASET;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.UNABLE_TO_CREATE_OR_UPDATE_DATASET;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.UNSUPPORTED_CONTENT;
import static org.talend.dataprep.i18n.DataprepBundle.message;
import static org.talend.dataprep.quality.AnalyzerService.Analysis.SEMANTIC;
import static org.talend.dataprep.util.SortAndOrderHelper.getDataSetMetadataComparator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetGovernance.Certification;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DatasetDTO;
import org.talend.dataprep.api.dataset.Import;
import org.talend.dataprep.api.dataset.Import.ImportBuilder;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.location.DataSetLocationService;
import org.talend.dataprep.api.dataset.location.LocalStoreLocation;
import org.talend.dataprep.api.dataset.location.locator.DataSetLocatorService;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.FlagNames;
import org.talend.dataprep.api.dataset.statistics.SemanticDomain;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.api.user.UserData;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCache.TimeToLive;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.dataset.event.DatasetImportedEvent;
import org.talend.dataprep.dataset.event.DatasetUpdatedEvent;
import org.talend.dataprep.dataset.service.analysis.synchronous.ContentAnalysis;
import org.talend.dataprep.dataset.service.analysis.synchronous.FormatAnalysis;
import org.talend.dataprep.dataset.service.analysis.synchronous.SchemaAnalysis;
import org.talend.dataprep.dataset.service.api.UpdateColumnParameters;
import org.talend.dataprep.dataset.service.cache.UpdateDataSetCacheKey;
import org.talend.dataprep.dataset.store.QuotaService;
import org.talend.dataprep.dataset.store.content.DataSetContentLimit;
import org.talend.dataprep.dataset.store.content.StrictlyBoundedInputStream;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.exception.json.JsonErrorCodeDescription;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.log.Markers;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;
import org.talend.dataprep.parameters.jsonschema.ComponentProperties;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.schema.DraftValidator;
import org.talend.dataprep.schema.FormatFamily;
import org.talend.dataprep.schema.FormatFamilyFactory;
import org.talend.dataprep.schema.Schema;
import org.talend.dataprep.security.PublicAPI;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.dataprep.user.store.UserDataRepository;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(value = "datasets", basePath = "/datasets", description = "Operations on data sets")
public class DataSetService extends BaseDataSetService {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataSetService.class);

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String STORAGE_LIMIT = "limit";

    /**
     * Format analyzer needed to update the schema.
     */
    @Autowired
    private FormatAnalysis formatAnalyzer;

    /**
     * User repository.
     */
    @Autowired
    private UserDataRepository userDataRepository;

    /**
     * Format guess factory.
     */
    @Autowired
    private FormatFamilyFactory formatFamilyFactory;

    /**
     * Dataset locator (used for remote datasets).
     */
    @Autowired
    private DataSetLocatorService datasetLocator;

    /**
     * DataPrep abstraction to the underlying security (whether it's enabled or not).
     */
    @Autowired
    private Security security;

    /**
     * All possible data set locations.
     */
    @Autowired
    private DataSetLocationService locationsService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private BeanConversionService conversionService;

    @Autowired
    private QuotaService quotaService;

    @Value("#{'${dataset.imports}'.split(',')}")
    private Set<String> enabledImports;

    @Value("${dataset.list.limit:10}")
    private int datasetListLimit;

    @Autowired
    private AnalyzerService analyzerService;

    @Autowired
    private FilterService filterService;

    @Value("${dataset.local.file.size.limit:20000000}")
    private long maximumInputStreamSize;

    @Autowired
    private ContentCache cacheManager;

    @Resource(name = "serializer#dataset#executor")
    private TaskExecutor executor;

    @Autowired
    private SecurityProxy securityProxy;

    @Autowired
    private BeanConversionService beanConversionService;

    @Autowired
    private DataSetContentLimit dataSetContentLimit;

    @RequestMapping(value = "/datasets", method = RequestMethod.GET)
    @ApiOperation(value = "List all data sets and filters on certified, or favorite or a limited number when asked",
            notes = "Returns the list of data sets (and filters) the current user is allowed to see. Creation date is a Epoch time value (in UTC time zone).")
    @Timed
    public Stream<DatasetDTO> list(
            @ApiParam(value = "Sort key (by name, creation or modification date)") @RequestParam(
                    defaultValue = "creationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc or modif)") @RequestParam(
                    defaultValue = "desc") Order order,
            @ApiParam(value = "Filter on name containing the specified name") @RequestParam(
                    required = false) String name,
            @ApiParam(value = "Filter on name containing the specified name strictness") @RequestParam(
                    defaultValue = "false") boolean nameStrict,
            @ApiParam(value = "Filter on certified data sets") @RequestParam(defaultValue = "false") boolean certified,
            @ApiParam(value = "Filter on favorite data sets") @RequestParam(defaultValue = "false") boolean favorite,
            @ApiParam(value = "Only return a limited number of data sets") @RequestParam(
                    defaultValue = "false") boolean limit) {

        String userId = security.getUserId();
        final UserData userData = userDataRepository.get(userId);

        Set<String> favorites = new HashSet<>();
        if (userData != null) {
            favorites = userData.getFavoritesDatasets();
        }

        Stream<DataSetMetadata> datasetList =
                findDataset(sort, order, name, nameStrict, certified, favorite, limit, favorites);

        Set<String> finalFavorites = favorites;
        return datasetList.map(p -> beanConversionService.convert(p, DatasetDTO.class, injectFavorite(finalFavorites)));
    }

    private BiFunction<DataSetMetadata, DatasetDTO, DatasetDTO> injectFavorite(Set<String> favoritesDatasets) {
        return (dataSetMetadata, datasetDTO) -> {
            datasetDTO.setFavorite(favoritesDatasets.contains(datasetDTO.getId()));
            return datasetDTO;
        };
    }

    @RequestMapping(value = "/datasets/details", method = RequestMethod.GET)
    @ApiOperation(value = "List all data sets and filters on certified, or favorite or a limited number when asked",
            notes = "Returns the list of data sets (and filters) the current user is allowed to see. Creation date is a Epoch time value (in UTC time zone).")
    @Timed
    public Stream<UserDataSetMetadata> listWithFullDetails(
            @ApiParam(value = "Sort key (by name, creation or modification date)") @RequestParam(
                    defaultValue = "creationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc or modif)") @RequestParam(
                    defaultValue = "desc") Order order,
            @ApiParam(value = "Filter on name containing the specified name") @RequestParam(
                    required = false) String name,
            @ApiParam(value = "Filter on name containing the specified name strictness") @RequestParam(
                    defaultValue = "false") boolean nameStrict,
            @ApiParam(value = "Filter on certified data sets") @RequestParam(defaultValue = "false") boolean certified,
            @ApiParam(value = "Filter on favorite data sets") @RequestParam(defaultValue = "false") boolean favorite,
            @ApiParam(value = "Only return a limited number of data sets") @RequestParam(
                    defaultValue = "false") boolean limit) {

        String userId = security.getUserId();
        final UserData userData = userDataRepository.get(userId);

        Set<String> favorites = new HashSet<>();
        if (userData != null) {
            favorites = userData.getFavoritesDatasets();
        }

        Stream<DataSetMetadata> datasetList =
                findDataset(sort, order, name, nameStrict, certified, favorite, limit, favorites);

        return datasetList.map(m -> conversionService.convert(m, UserDataSetMetadata.class));

    }

    /**
     * Returns a list containing all data sets that are compatible with the data set with id <tt>dataSetId</tt>. If no
     * compatible data set is found an empty list is returned. The data set with id <tt>dataSetId</tt> is never returned
     * in the list.
     *
     * @param dataSetId the specified data set id
     * @param sort the sort criterion: either name or date.
     * @param order the sorting order: either asc or desc.
     * @return a list containing all data sets that are compatible with the data set with id <tt>dataSetId</tt> and
     * empty list if no data set is compatible.
     */
    @RequestMapping(value = "/datasets/{id}/compatibledatasets", method = RequestMethod.GET)
    @ApiOperation(value = "List all compatible data sets",
            notes = "Returns the list of data sets the current user is allowed to see and which are compatible with the specified data set id.")
    @Timed
    public Iterable<UserDataSetMetadata> listCompatibleDatasets(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set metadata") String dataSetId,
            @ApiParam(value = "Sort key (by name or date).") @RequestParam(defaultValue = "creationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc).") @RequestParam(defaultValue = "desc") Order order) {

        Spliterator<DataSetMetadata> iterator = dataSetMetadataRepository.listCompatible(dataSetId).spliterator();

        final Comparator<DataSetMetadata> comparator = getDataSetMetadataComparator(sort, order);

        // Return sorted results
        try (Stream<DataSetMetadata> stream = stream(iterator, false)) {
            return stream
                    .filter(metadata -> !metadata.getLifecycle().isImporting()) //
                    .map(m -> conversionService.convert(m, UserDataSetMetadata.class)) //
                    .sorted(comparator) //
                    .collect(Collectors.toList());
        }
    }

    /**
     * Creates a new data set and returns the new data set id as text in the response.
     *
     * @param name An optional name for the new data set (might be <code>null</code>).
     * @param size An optional size for the newly created data set.
     * @param contentType the request content type.
     * @param content The raw content of the data set (might be a CSV, XLS...) or the connection parameter in case of a
     * remote csv.
     * @return The new data id.
     * @see DataSetService#get(boolean, boolean, long, String, String)
     */
    //@formatter:off
    @RequestMapping(value = "/datasets", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a data set", produces = TEXT_PLAIN_VALUE, notes = "Create a new data set based on content provided in POST body. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too. Returns the id of the newly created data set.")
    @Timed
    @VolumeMetered
    public String create(
            @ApiParam(value = "User readable name of the data set (e.g. 'Finance Report 2015', 'Test Data Set').") @RequestParam(defaultValue = "") String name,
            @ApiParam(value = "An optional tag to be added in data set metadata once created.") @RequestParam(defaultValue = "") String tag,
            @ApiParam(value = "Size of the data set, in bytes.") @RequestParam(required = false) Long size,
            @RequestHeader(CONTENT_TYPE) String contentType,
            @ApiParam(value = "content") InputStream content) {
        //@formatter:on
        checkDataSetName(name);

        final String id = UUID.randomUUID().toString();
        final Marker marker = Markers.dataset(id);
        LOG.debug(marker, "Creating...");

        // sanity check
        if (size != null && size < 0) {
            LOG.warn("invalid size provided {}", size);
            throw new TDPException(UNEXPECTED_CONTENT, build().put("size", size));
        }

        // check that the name is not already taken
        checkIfNameIsAvailable(name);

        // get the location out of the content type and the request body
        final DataSetLocation location;
        try {
            location = datasetLocator.getDataSetLocation(contentType, content);
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_LOCATION, e);
        }
        DataSetMetadata dataSetMetadata = null;
        final TDPException hypotheticalException;
        try {

            // if the size is provided, let's check if the quota will not be exceeded
            if (size != null && size > 0) {
                quotaService.checkIfAddingSizeExceedsAvailableStorage(size);
            }

            dataSetMetadata = metadataBuilder //
                    .metadata() //
                    .id(id) //
                    .name(name) //
                    .author(security.getUserId()) //
                    .location(location) //
                    .created(System.currentTimeMillis()) //
                    .tag(tag) //
                    .build();

            dataSetMetadata.getLifecycle().setImporting(true); // Indicate data set is being imported

            // Save data set content
            LOG.debug(marker, "Storing content...");
            final long maxDataSetSizeAllowed = getMaxDataSetSizeAllowed();
            final StrictlyBoundedInputStream sizeCalculator =
                    new StrictlyBoundedInputStream(content, maxDataSetSizeAllowed);
            contentStore.storeAsRaw(dataSetMetadata, sizeCalculator);
            dataSetMetadata.setDataSetSize(sizeCalculator.getTotal());
            LOG.debug(marker, "Content stored.");

            // Create the new data set
            dataSetMetadataRepository.save(dataSetMetadata);
            LOG.debug(marker, "dataset metadata stored {}", dataSetMetadata);

            // Queue events (format analysis, content indexing for search...)
            analyzeDataSet(id, emptyList());

            LOG.debug(marker, "Created!");

            // TDP-6141 : Do not send `DataSteImportedEvent` synchronously because it will do the same analyse as before
            // a second time and can reach timeout
            asyncPublisher.multicastEvent(new DatasetImportedEvent(id));

            return id;
        } catch (StrictlyBoundedInputStream.InputStreamTooLargeException e) {
            hypotheticalException =
                    new TDPException(MAX_STORAGE_MAY_BE_EXCEEDED, e, build().put(STORAGE_LIMIT, e.getMaxSize()));
        } catch (TDPException e) {
            hypotheticalException = e;
        } catch (Exception e) {
            hypotheticalException = new TDPException(UNABLE_CREATE_DATASET, e);
        } finally {
            // because the client might still be writing the request content, closing the connexion right now
            // might end up in a 'connection reset' or a 'broken pipe' error in API.
            //
            // So, let's read fully the request content before closing the connection.
            dataSetContentToNull(content);
        }
        // remove dataSetMetadata
        securityProxy.asTechnicalUser();
        try {
            dataSetMetadataRepository.remove(id);
        } finally {
            securityProxy.releaseIdentity();
        }

        if (dataSetMetadata != null) {
            try {
                contentStore.delete(dataSetMetadata);
            } catch (Exception e) {
                LOG.error("Unable to delete uploaded data.", e);
            }
        }
        throw hypotheticalException;
    }

    /**
     * Returns the <b>full</b> data set content for given id.
     *
     * @param metadata If <code>true</code>, includes data set metadata information.
     * @param dataSetId A data set id.
     * @return The full data set.
     */
    @RequestMapping(value = "/datasets/{id}/content", method = RequestMethod.GET)
    @ApiOperation(value = "Get a data set by id",
            notes = "Get a data set content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content.")
    @Timed
    @ResponseBody
    public Callable<DataSet> get(
            @RequestParam(defaultValue = "true") @ApiParam(name = "metadata",
                    value = "Include metadata information in the response") boolean metadata, //
            @RequestParam(defaultValue = "false") @ApiParam(name = "includeInternalContent",
                    value = "Include internal content in the response") boolean includeInternalContent, //
            @RequestParam(defaultValue = "-1") @ApiParam(name = STORAGE_LIMIT, value = STORAGE_LIMIT) long limit, //
            @ApiParam(value = "Filter for retrieved content.") @RequestParam(value = "filter",
                    defaultValue = "") String filter,
            @PathVariable(value = "id") @ApiParam(name = "id",
                    value = "Id of the requested data set") String dataSetId) {
        return () -> {
            final Marker marker = Markers.dataset(dataSetId);
            LOG.debug(marker, "Get data set #{}", dataSetId);
            Stream<DataSetRow> stream = null;
            try {
                DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
                assertDataSetMetadata(dataSetMetadata, dataSetId);
                // Build the result
                DataSet dataSet = new DataSet();
                if (metadata) {
                    dataSet.setMetadata(conversionService.convert(dataSetMetadata, UserDataSetMetadata.class));
                }
                stream = contentStore.stream(dataSetMetadata, limit); // Disable line limit

                // on-demand analyzer for dataset (See TDP-4404, migration problems)
                if (dataSetMetadata.getRowMetadata().getColumns().stream().anyMatch(
                        c -> c.getStatistics().getWordPatternFrequencyTable().isEmpty())) {
                    stream = insertWordPatternAnalysis(dataSetMetadata, stream);
                }

                if (!includeInternalContent) {
                    LOG.debug("Skip internal content when serving data set #{} content.", dataSetId);
                    stream = stream.map(r -> {
                        final Map<String, Object> values = r.values();
                        final Map<String, Object> filteredValues = new HashMap<>(values);
                        // Remove technical properties from returned values.
                        values.forEach((k, v) -> {
                            if (k != null && k.startsWith(FlagNames.INTERNAL_PROPERTY_PREFIX)) {
                                filteredValues.remove(k);
                            }
                        });
                        filteredValues.put(FlagNames.TDP_ID, r.getTdpId()); // Include TDP_ID anyway
                        return new DataSetRow(r.getRowMetadata(), filteredValues);
                    });
                }

                // Filter content
                stream = stream.filter(filterService.build(filter, dataSetMetadata.getRowMetadata()));

                dataSet.setRecords(stream);
                return dataSet;
            } catch (Exception e) {
                if (stream != null) {
                    stream.close();
                }
                throw e;
            } finally {
                LOG.debug(marker, "Get done.");
            }
        };
    }

    private Stream<DataSetRow> insertWordPatternAnalysis(DataSetMetadata dataSetMetadata, Stream<DataSetRow> records) {
        // recompute both patterns because TDQ has change char pattern detection precision at the same time they added
        // word pattern
        Analyzer<Analyzers.Result> wordPatternAnalyzer =
                analyzerService.build(dataSetMetadata.getRowMetadata().getColumns(),
                        AnalyzerService.Analysis.WORD_PATTERNS, AnalyzerService.Analysis.PATTERNS);

        final AtomicInteger analyzerCount = new AtomicInteger(0);
        String datasetId = dataSetMetadata.getId();
        Long limit = dataSetContentLimit.getLimit();
        Predicate<Integer> limitPredicate = limit == null ? i -> true : i -> i < limit;

        return records.peek(r -> {
            if (limitPredicate.test(analyzerCount.get())) {
                wordPatternAnalyzer.analyze(r.toArray());
                analyzerCount.incrementAndGet();
            }
        }).onClose(() -> {
            wordPatternAnalyzer.end();
            final List<Analyzers.Result> analyzerResult = wordPatternAnalyzer.getResult();
            DataSetMetadata dataSetMetadataInner = dataSetMetadataRepository.get(datasetId);
            final StatisticsAdapter statisticsAdapter = new StatisticsAdapter(40);
            statisticsAdapter.adapt(dataSetMetadataInner.getRowMetadata().getColumns(), analyzerResult);
            dataSetMetadataRepository.save(dataSetMetadataInner);
        });
    }

    /**
     * Returns the data set {@link DataSetMetadata metadata} for given <code>dataSetId</code>.
     *
     * @param dataSetId A data set id. If <code>null</code> <b>or</b> if no data set with provided id exits, operation
     * returns {@link org.apache.http.HttpStatus#SC_NO_CONTENT} if metadata does not exist.
     */
    @RequestMapping(value = "/datasets/{id}/metadata", method = RequestMethod.GET)
    @ApiOperation(value = "Get metadata information of a data set by id",
            notes = "Get metadata information of a data set by id. Not valid or non existing data set id returns empty content.")
    @Timed
    @ResponseBody
    public DataSet getMetadata(@PathVariable(value = "id") @ApiParam(name = "id",
            value = "Id of the data set metadata") String dataSetId) {
        if (dataSetId == null) {
            HttpResponseContext.status(HttpStatus.NO_CONTENT);
            return null;
        }

        LOG.debug("get dataset metadata for {}", dataSetId);

        DataSetMetadata metadata;
        securityProxy.asTechnicalUserForDataSet();
        try {
            metadata = dataSetMetadataRepository.get(dataSetId);
        } finally {
            securityProxy.releaseIdentity();
        }
        if (metadata == null) {
            throw new TDPException(DataSetErrorCodes.DATASET_DOES_NOT_EXIST, build().put("id", dataSetId));
        }

        if (!metadata.getLifecycle().schemaAnalyzed()) {
            LOG.warn("Schema analyzed is not finished for #{}", dataSetId);
        }

        DataSet dataSet = new DataSet();
        dataSet.setMetadata(conversionService.convert(metadata, UserDataSetMetadata.class));
        LOG.debug("found dataset {} for #{}", dataSet.getMetadata().getName(), dataSetId);
        return dataSet;
    }

    /**
     * Deletes a data set with provided id.
     *
     * @param dataSetId A data set id. If data set id is unknown, no exception nor status code to indicate this is set.
     */
    @RequestMapping(value = "/datasets/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete a data set by id",
            notes = "Delete a data set content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content.")
    @Timed
    public void delete(@PathVariable(value = "id") @ApiParam(name = "id",
            value = "Id of the data set to delete") String dataSetId) {
        DataSetMetadata metadata = dataSetMetadataRepository.get(dataSetId);
        final DistributedLock lock = dataSetMetadataRepository.createDatasetMetadataLock(dataSetId);
        try {
            lock.lock();
            // you can't delete a Dataset that is not completely updated
            if (metadata != null) {
                // first remove the metadata as there may be additional check
                dataSetMetadataRepository.remove(dataSetId);
                contentStore.delete(metadata);
            } else {
                HttpResponseContext.status(HttpStatus.NOT_FOUND);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Copy this dataset to a new one and returns the new data set id as text in the response.
     *
     * @param copyName the name of the copy
     * @return The new data id.
     */
    @RequestMapping(value = "/datasets/{id}/copy", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Copy a data set", produces = TEXT_PLAIN_VALUE,
            notes = "Copy a new data set based on the given id. Returns the id of the newly created data set.")
    @Timed
    public String copy(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to clone") String dataSetId,
            @ApiParam(value = "The name of the cloned dataset.") @RequestParam(required = false) String copyName)
            throws IOException {

        if (copyName != null) {
            checkDataSetName(copyName);
        }

        HttpResponseContext.contentType(TEXT_PLAIN_VALUE);

        DataSetMetadata original = dataSetMetadataRepository.get(dataSetId);
        if (original == null) {
            return StringUtils.EMPTY;
        }

        // use a default name if empty (original name + " Copy" )
        final String newName;
        if (StringUtils.isBlank(copyName)) {
            newName = message("dataset.copy.newname", original.getName());
        } else {
            newName = copyName;
        }

        final DistributedLock lock = dataSetMetadataRepository.createDatasetMetadataLock(dataSetId);
        try {
            lock.lock(); // lock to ensure any asynchronous analysis is completed.

            // check that the name is not already taken
            checkIfNameIsAvailable(newName);

            // check that there's enough space
            final long maxDataSetSizeAllowed = getMaxDataSetSizeAllowed();
            if (maxDataSetSizeAllowed < original.getDataSetSize()) {
                throw new TDPException(MAX_STORAGE_MAY_BE_EXCEEDED, build().put(STORAGE_LIMIT, maxDataSetSizeAllowed));
            }

            // Create copy (based on original data set metadata)
            final String newId = UUID.randomUUID().toString();
            final Marker marker = Markers.dataset(newId);
            LOG.debug(marker, "Cloning...");
            DataSetMetadata target = metadataBuilder //
                    .metadata() //
                    .copy(original) //
                    .id(newId) //
                    .name(newName) //
                    .author(security.getUserId()) //
                    .location(original.getLocation()) //
                    .created(System.currentTimeMillis()) //
                    .build();

            // Save data set content
            LOG.debug(marker, "Storing content...");
            try (InputStream content = contentStore.getAsRaw(original)) {
                contentStore.storeAsRaw(target, content);
            }

            LOG.debug(marker, "Content stored.");

            // Create the new data set
            dataSetMetadataRepository.save(target);

            LOG.info(marker, "Copy done --> {}", newId);

            return newId;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Updates a data set content and metadata. If no data set exists for given id, data set is silently created.
     *
     * @param dataSetId The id of data set to be updated.
     * @param name The new name for the data set. Empty name (or <code>null</code>) does not update dataset name.
     * @param dataSetContent The new content for the data set. If empty, existing content will <b>not</b> be replaced.
     * For delete operation, look at {@link #delete(String)}.
     */
    @RequestMapping(value = "/datasets/{id}/raw", method = PUT)
    @ApiOperation(value = "Update a data set by id",
            notes = "Update a data set content based on provided id and PUT body. Id should be a UUID returned by the list operation. Not valid or non existing data set id returns empty content. For documentation purposes, body is typed as 'text/plain' but operation accepts binary content too.")
    @Timed
    @VolumeMetered
    public String updateRawDataSet(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to update") String dataSetId, //
            @RequestParam(value = "name", required = false) @ApiParam(name = "name",
                    value = "New value for the data set name") String name, //
            @RequestParam(value = "size", required = false) @ApiParam(name = "size",
                    value = "The size of the dataSet") Long size, //
            @ApiParam(value = "content") InputStream dataSetContent) {

        LOG.debug("updating dataset content #{}", dataSetId);

        if (name != null) {
            checkDataSetName(name);
        }

        DataSetMetadata currentDataSetMetadata = dataSetMetadataRepository.get(dataSetId);

        if (currentDataSetMetadata == null) {
            return create(name, null, size, TEXT_PLAIN_VALUE, dataSetContent);
        } else {

            // just like the creation, let's make sure invalid size forbids dataset creation
            if (size != null && size < 0) {
                LOG.warn("invalid size provided {}", size);
                throw new TDPException(UNSUPPORTED_CONTENT);
            }

            final UpdateDataSetCacheKey cacheKey = new UpdateDataSetCacheKey(currentDataSetMetadata.getId());

            final DistributedLock lock =
                    dataSetMetadataRepository.createDatasetMetadataLock(currentDataSetMetadata.getId());
            try {
                lock.lock();

                // check the size if it's available (quick win)
                if (size != null && size > 0) {
                    quotaService.checkIfAddingSizeExceedsAvailableStorage(
                            Math.abs(size - currentDataSetMetadata.getDataSetSize()));
                }

                final DataSetMetadataBuilder datasetBuilder =
                        metadataBuilder.metadata().id(currentDataSetMetadata.getId());
                datasetBuilder.copyNonContentRelated(currentDataSetMetadata);
                datasetBuilder.modified(System.currentTimeMillis());
                if (!StringUtils.isEmpty(name)) {
                    datasetBuilder.name(name);
                }
                final DataSetMetadata updatedDataSetMetadata = datasetBuilder.build();

                // Save data set content into cache to make sure there's enough space in the content store
                final long maxDataSetSizeAllowed = getMaxDataSetSizeAllowed();
                final StrictlyBoundedInputStream sizeCalculator =
                        new StrictlyBoundedInputStream(dataSetContent, maxDataSetSizeAllowed);
                try (OutputStream cacheEntry = cacheManager.put(cacheKey, TimeToLive.DEFAULT)) {
                    IOUtils.copy(sizeCalculator, cacheEntry);
                }

                // once fully copied to the cache, we know for sure that the content store has enough space, so let's
                // copy
                // from the cache to the content store
                PipedInputStream toContentStore = new PipedInputStream();
                PipedOutputStream fromCache = new PipedOutputStream(toContentStore);
                Runnable r = () -> {
                    try (final InputStream input = cacheManager.get(cacheKey)) {
                        IOUtils.copy(input, fromCache);
                        fromCache.close(); // it's important to close this stream, otherwise the piped stream will never
                                           // close
                    } catch (IOException e) {
                        throw new TDPException(UNABLE_TO_CREATE_OR_UPDATE_DATASET, e);
                    }
                };
                executor.execute(r);
                contentStore.storeAsRaw(updatedDataSetMetadata, toContentStore);

                // update the dataset metadata with its new size
                updatedDataSetMetadata.setDataSetSize(sizeCalculator.getTotal());
                dataSetMetadataRepository.save(updatedDataSetMetadata);

                // Content was changed, so queue events (format analysis, content indexing for search...)
                analyzeDataSet(currentDataSetMetadata.getId(), emptyList());

            } catch (StrictlyBoundedInputStream.InputStreamTooLargeException e) {
                LOG.warn("Dataset update {} cannot be done, new content is too big", currentDataSetMetadata.getId());
                throw new TDPException(MAX_STORAGE_MAY_BE_EXCEEDED, e, build().put(STORAGE_LIMIT, e.getMaxSize()));
            } catch (IOException e) {
                LOG.error("Error updating the dataset", e);
                throw new TDPException(UNABLE_TO_CREATE_OR_UPDATE_DATASET, e);
            } finally {
                dataSetContentToNull(dataSetContent);
                // whatever the outcome the cache needs to be cleaned
                if (cacheManager.has(cacheKey)) {
                    cacheManager.evict(cacheKey);
                }
                lock.unlock();
            }

            // publishing update event
            publisher.publishEvent(new DatasetUpdatedEvent(currentDataSetMetadata));

            return currentDataSetMetadata.getId();
        }
    }

    /**
     * Fully read the given input stream to /dev/null.
     *
     * @param content some content
     */
    private void dataSetContentToNull(InputStream content) {
        try {
            IOUtils.copy(content, new NullOutputStream());
        } catch (IOException ioe) {
            // no op
        }
    }

    /**
     * List all dataset related error codes.
     */
    @RequestMapping(value = "/datasets/errors", method = RequestMethod.GET)
    @ApiOperation(value = "Get all dataset related error codes.",
            notes = "Returns the list of all dataset related error codes.")
    @Timed
    public Stream<JsonErrorCodeDescription> listErrors() {
        return Arrays.stream(DataSetErrorCodes.values()).map(JsonErrorCodeDescription::new);
    }

    /**
     * Returns preview of the the data set content for given id (first 100 rows). Service might return
     * {@link org.apache.http.HttpStatus#SC_ACCEPTED} if the data set exists but analysis is not yet fully
     * completed so content is not yet ready to be served.
     *
     * @param metadata If <code>true</code>, includes data set metadata information.
     * @param sheetName the sheet name to preview
     * @param dataSetId A data set id.
     */
    @RequestMapping(value = "/datasets/{id}/preview", method = RequestMethod.GET)
    @ApiOperation(value = "Get a data preview set by id",
            notes = "Get a data set preview content based on provided id. Not valid or non existing data set id returns empty content. Data set not in drat status will return a redirect 301")
    @Timed
    @ResponseBody
    public DataSet
            preview(@RequestParam(defaultValue = "true") @ApiParam(name = "metadata",
                    value = "Include metadata information in the response") boolean metadata, //
                    @RequestParam(defaultValue = "") @ApiParam(name = "sheetName",
                            value = "Sheet name to preview") String sheetName, //
                    @PathVariable(value = "id") @ApiParam(name = "id",
                            value = "Id of the requested data set") String dataSetId) {

        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);

        if (dataSetMetadata == null) {
            HttpResponseContext.status(HttpStatus.NO_CONTENT);
            return DataSet.empty(); // No data set, returns empty content.
        }
        if (!dataSetMetadata.isDraft()) {
            // Moved to get data set content operation
            HttpResponseContext.status(HttpStatus.MOVED_PERMANENTLY);
            HttpResponseContext.header("Location", "/datasets/" + dataSetId + "/content");
            return DataSet.empty(); // dataset not anymore a draft so preview doesn't make sense.
        }
        if (StringUtils.isNotEmpty(sheetName)) {
            dataSetMetadata.setSheetName(sheetName);
        }
        // take care of previous data without schema parser result
        if (dataSetMetadata.getSchemaParserResult() != null) {
            // sheet not yet set correctly so use the first one
            if (StringUtils.isEmpty(dataSetMetadata.getSheetName())) {
                String theSheetName = dataSetMetadata.getSchemaParserResult().getSheetContents().get(0).getName();
                LOG.debug("preview for dataSetMetadata: {} with sheetName: {}", dataSetId, theSheetName);
                dataSetMetadata.setSheetName(theSheetName);
            }

            String theSheetName = dataSetMetadata.getSheetName();

            Optional<Schema.SheetContent> sheetContentFound = dataSetMetadata
                    .getSchemaParserResult() //
                    .getSheetContents() //
                    .stream() //
                    .filter(sheetContent -> theSheetName.equals(sheetContent.getName())) //
                    .findFirst();

            if (!sheetContentFound.isPresent()) {
                HttpResponseContext.status(HttpStatus.NO_CONTENT);
                return DataSet.empty(); // No sheet found, returns empty content.
            }

            List<ColumnMetadata> columnMetadatas = sheetContentFound.get().getColumnMetadatas();

            if (dataSetMetadata.getRowMetadata() == null) {
                dataSetMetadata.setRowMetadata(new RowMetadata(emptyList()));
            }

            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);
        } else {
            LOG.warn("dataset#{} has draft status but any SchemaParserResult", dataSetId);
        }
        // Build the result
        DataSet dataSet = new DataSet();
        if (metadata) {
            dataSet.setMetadata(conversionService.convert(dataSetMetadata, UserDataSetMetadata.class));
        }
        dataSet.setRecords(contentStore.stream(dataSetMetadata).limit(100));
        return dataSet;
    }

    /**
     * Updates a data set metadata. If no data set exists for given id, a {@link TDPException} is thrown.
     *
     * @param dataSetId The id of data set to be updated.
     * @param dataSetMetadata The new content for the data set. If empty, existing content will <b>not</b> be replaced.
     * For delete operation, look at {@link #delete(String)}.
     */
    @RequestMapping(value = "/datasets/{id}", method = PUT)
    @ApiOperation(value = "Update a data set metadata by id",
            notes = "Update a data set metadata according to the content of the PUT body. Id should be a UUID returned by the list operation. Not valid or non existing data set id return an error response.")
    @Timed
    public void updateDataSet(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the data set to update") String dataSetId,
            @RequestBody DataSetMetadata dataSetMetadata) {

        if (dataSetMetadata != null && dataSetMetadata.getName() != null) {
            checkDataSetName(dataSetMetadata.getName());
        }

        final DistributedLock lock = dataSetMetadataRepository.createDatasetMetadataLock(dataSetId);
        lock.lock();
        try {

            DataSetMetadata metadataForUpdate = dataSetMetadataRepository.get(dataSetId);
            if (metadataForUpdate == null) {
                // No need to silently create the data set metadata: associated content will most likely not exist.
                throw new TDPException(DataSetErrorCodes.DATASET_DOES_NOT_EXIST, build().put("id", dataSetId));
            }

            LOG.debug("updateDataSet: {}", dataSetMetadata);

            //
            // Only part of the metadata can be updated, so the original dataset metadata is loaded and updated
            //

            DataSetMetadata original = metadataBuilder.metadata().copy(metadataForUpdate).build();
            try {
                // update the name
                metadataForUpdate.setName(dataSetMetadata.getName());

                // update the sheet content (in case of a multi-sheet excel file)
                if (metadataForUpdate.getSchemaParserResult() != null) {
                    Optional<Schema.SheetContent> sheetContentFound =
                            metadataForUpdate
                                    .getSchemaParserResult() //
                                    .getSheetContents() //
                                    .stream()
                                    .filter(sheetContent -> dataSetMetadata
                                            .getSheetName()
                                            .equals(sheetContent.getName())) //
                                    .findFirst();

                    if (sheetContentFound.isPresent()) {
                        List<ColumnMetadata> columnMetadatas = sheetContentFound.get().getColumnMetadatas();
                        if (metadataForUpdate.getRowMetadata() == null) {
                            metadataForUpdate.setRowMetadata(new RowMetadata(emptyList()));
                        }
                        metadataForUpdate.getRowMetadata().setColumns(columnMetadatas);
                    }

                    metadataForUpdate.setSheetName(dataSetMetadata.getSheetName());
                    metadataForUpdate.setSchemaParserResult(null);
                }

                // Location updates
                if (dataSetMetadata.getLocation() != null) {
                    metadataForUpdate.setLocation(dataSetMetadata.getLocation());
                }

                // update parameters & encoding (so that user can change import parameters for CSV)
                metadataForUpdate.getContent().setParameters(dataSetMetadata.getContent().getParameters());
                metadataForUpdate.setEncoding(dataSetMetadata.getEncoding());

                // update limit
                final Optional<Long> newLimit = dataSetMetadata.getContent().getLimit();
                newLimit.ifPresent(limit -> metadataForUpdate.getContent().setLimit(limit));

                // Validate that the new data set metadata and removes the draft status
                final String formatFamilyId = dataSetMetadata.getContent().getFormatFamilyId();
                if (formatFamilyFactory.hasFormatFamily(formatFamilyId)) {
                    FormatFamily format = formatFamilyFactory.getFormatFamily(formatFamilyId);
                    try {
                        DraftValidator draftValidator = format.getDraftValidator();
                        DraftValidator.Result result = draftValidator.validate(dataSetMetadata);
                        if (result.isDraft()) {
                            // This is not an exception case: data set may remain a draft after update (although rather
                            // unusual)
                            LOG.warn("Data set #{} is still a draft after update.", dataSetId);
                            return;
                        }
                        // Data set metadata to update is no longer a draft
                        metadataForUpdate.setDraft(false);
                    } catch (UnsupportedOperationException e) {
                        // no need to validate draft here
                    }
                }

                // update schema
                formatAnalyzer.update(original, metadataForUpdate);

                // save the result
                metadataForUpdate.getLifecycle().setInProgress(true);
                metadataForUpdate.getContent().setNbRecords(0);
                dataSetMetadataRepository.save(metadataForUpdate);

                // Asks for a in depth schema analysis (for column type information).
                analyzeDataSet(dataSetId, singletonList(FormatAnalysis.class));
            } catch (TDPException e) {
                throw e;
            } catch (Exception e) {
                throw new TDPException(UNABLE_TO_CREATE_OR_UPDATE_DATASET, e);
            }
        } finally {
            lock.unlock();
        }
        publisher.publishEvent(new DatasetUpdatedEvent(dataSetMetadata));
    }

    /**
     * list all the favorites dataset for the current user
     *
     * @return a list of the dataset Ids of all the favorites dataset for the current user or an empty list if none
     * found
     */
    @RequestMapping(value = "/datasets/favorites", method = RequestMethod.GET)
    @ApiOperation(value = "return all favorites datasets of the current user",
            notes = "Returns the list of favorites datasets.")
    @Timed
    public Iterable<String> favorites() {
        String userId = security.getUserId();
        UserData userData = userDataRepository.get(userId);
        return userData != null ? userData.getFavoritesDatasets() : emptyList();
    }

    /**
     * update the current user data dataset favorites list by adding or removing the dataSetId according to the unset
     * flag. The user data for the current will be created if it does not exist. If no data set exists for given id, a
     * {@link TDPException} is thrown.
     *
     * @param unset, if true this will remove the dataSetId from the list of favorites, if false then it adds the
     * dataSetId to the favorite list
     * @param dataSetId, the id of the favorites data set. If the data set does not exists nothing is done.
     */
    @RequestMapping(value = "/datasets/{id}/favorite", method = PUT)
    @ApiOperation(value = "set or unset a dataset as favorite",
            notes = "Specify if a dataset is or is not a favorite for the current user.")
    @Timed
    public void setFavorites(@RequestParam(defaultValue = "false") @ApiParam(name = "unset",
            value = "if true then unset the dataset as favorite, if false (default value) set the favorite flag") boolean unset, //
            @PathVariable(value = "id") @ApiParam(name = "id",
                    value = "Id of the favorite data set, do nothing is the id does not exist.") String dataSetId) {
        String userId = security.getUserId();
        // check that dataset exists
        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        if (dataSetMetadata != null) {
            LOG.debug("{} favorite dataset for #{} for user {}", unset ? "Unset" : "Set", dataSetId, userId); // $NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

            UserData userData = userDataRepository.get(userId);
            if (unset) { // unset the favorites
                if (userData != null) {
                    userData.getFavoritesDatasets().remove(dataSetId);
                    userDataRepository.save(userData);
                } // no user data for this user so nothing to unset
            } else { // set the favorites
                if (userData == null) { // let's create a new UserData
                    userData = new UserData(userId, versionService.version().getVersionId());
                } // else already created so just update it.
                userData.addFavoriteDataset(dataSetId);
                userDataRepository.save(userData);
            }
        } else { // no dataset found so throws an error
            throw new TDPException(DataSetErrorCodes.DATASET_DOES_NOT_EXIST, build().put("id", dataSetId));
        }
    }

    /**
     * Update the column of the data set and computes the
     *
     * @param dataSetId the dataset id.
     * @param columnId the column id.
     * @param parameters the new type and domain.
     */
    @RequestMapping(value = "/datasets/{datasetId}/column/{columnId}", method = POST)
    @ApiOperation(value = "Update a column type and/or domain")
    @Timed
    public void updateDatasetColumn(
            @PathVariable(value = "datasetId") @ApiParam(name = "datasetId",
                    value = "Id of the dataset") final String dataSetId,
            @PathVariable(value = "columnId") @ApiParam(name = "columnId",
                    value = "Id of the column") final String columnId,
            @RequestBody final UpdateColumnParameters parameters) {

        final DistributedLock lock = dataSetMetadataRepository.createDatasetMetadataLock(dataSetId);
        lock.lock();
        try {

            // check that dataset exists
            final DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
            if (dataSetMetadata == null) {
                throw new TDPException(DataSetErrorCodes.DATASET_DOES_NOT_EXIST, build().put("id", dataSetId));
            }

            LOG.debug("update dataset column for #{} with type {} and/or domain {}", dataSetId, parameters.getType(),
                    parameters.getDomain());

            // get the column
            final ColumnMetadata column = dataSetMetadata.getRowMetadata().getById(columnId);
            if (column == null) {
                throw new TDPException(DataSetErrorCodes.COLUMN_DOES_NOT_EXIST, //
                        build() //
                                .put("id", dataSetId) //
                                .put("columnid", columnId));
            }

            // update type/domain
            if (parameters.getType() != null) {
                column.setType(parameters.getType());
            }
            if (parameters.getDomain() != null) {
                // erase domain to let only type
                if (parameters.getDomain().isEmpty()) {
                    column.setDomain("");
                    column.setDomainLabel("");
                    column.setDomainFrequency(0);
                }
                // change domain
                else {
                    final SemanticDomain semanticDomain = column //
                            .getSemanticDomains() //
                            .stream() //
                            .filter(dom -> StringUtils.equals(dom.getId(), parameters.getDomain())) //
                            .findFirst() //
                            .orElse(null);
                    if (semanticDomain != null) {
                        column.setDomain(semanticDomain.getId());
                        column.setDomainLabel(semanticDomain.getLabel());
                        column.setDomainFrequency(semanticDomain.getScore());
                    }
                }
            }

            // save
            dataSetMetadataRepository.save(dataSetMetadata);

            // analyze the updated dataset (not all analysis are performed)
            analyzeDataSet(dataSetId, //
                    //
                    asList(ContentAnalysis.class, FormatAnalysis.class, SchemaAnalysis.class));

        } finally {
            lock.unlock();
        }
    }

    /**
     * Search datasets.
     *
     * @param name what to searched in datasets.
     * @param strict If the searched name should be the full name
     * @return the list of found datasets metadata.
     * @deprecated please, use {@link #list(Sort, Order, String, boolean, boolean, boolean, boolean)} on
     * {@code /datasets} enpoint
     * with name and nameStrict parameters.
     */
    @RequestMapping(value = "/datasets/search", method = GET)
    @ApiOperation(value = "Search the dataset metadata", notes = "Search the dataset metadata.")
    @Timed
    @Deprecated
    public Stream<DatasetDTO> search( //
            @RequestParam @ApiParam(value = "What to search in data sets") final String name, //
            @RequestParam @ApiParam(value = "The searched name should be the full name") final boolean strict) {
        LOG.debug("search datasets metadata for {}", name);
        return list(null, null, name, strict, false, false, false);
    }

    @RequestMapping(value = "/datasets/imports/{import}/parameters", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the import parameters",
            notes = "This list can be used by user to change dataset encoding.")
    @Timed
    @PublicAPI
    // This method have to return Object because it can either return the legacy List<Parameter> or the new TComp
    // oriented
    // ComponentProperties
    public Object getImportParameters(@PathVariable("import") final String importType) {
        DataSetLocation matchingDatasetLocation = locationsService.findLocation(importType);
        Object parametersToReturn;
        if (matchingDatasetLocation == null) {
            parametersToReturn = emptyList();
        } else {
            if (matchingDatasetLocation.isSchemaOriented()) {
                parametersToReturn = matchingDatasetLocation.getParametersAsSchema(getLocale());
            } else {
                parametersToReturn = matchingDatasetLocation.getParameters(getLocale());
            }
        }
        return parametersToReturn;
    }

    @RequestMapping(value = "/datasets/{id}/datastore/properties", method = GET)
    @ApiOperation(value = "Get the dataset import parameters",
            notes = "This list can be used by user to change dataset encoding.")
    @Timed
    // This method have to return Object because it can either return the legacy List<Parameter> or the new TComp
    // oriented
    // ComponentProperties
    public Object getDataStoreParameters(@PathVariable("id") final String dataSetId) {
        DataSetMetadata dataSetMetadata = dataSetMetadataRepository.get(dataSetId);
        Object parametersToReturn = null;
        if (dataSetMetadata != null) {
            DataSetLocation matchingDatasetLocation =
                    locationsService.findLocation(dataSetMetadata.getLocation().getLocationType());
            if (matchingDatasetLocation == null) {
                parametersToReturn = emptyList();
            } else {
                if (matchingDatasetLocation.isSchemaOriented()) {
                    ComponentProperties parametersAsSchema = matchingDatasetLocation.getParametersAsSchema(getLocale());
                    parametersAsSchema.setProperties(
                            dataSetMetadata.getLocation().getParametersAsSchema(getLocale()).getProperties());
                    parametersToReturn = parametersAsSchema;
                } else {
                    parametersToReturn = matchingDatasetLocation.getParameters(getLocale());
                }
            }
        }
        return parametersToReturn;
    }

    @RequestMapping(value = "/datasets/imports", method = GET)
    @ApiOperation(value = "list the supported encodings for dataset",
            notes = "This list can be used by user to change dataset encoding.")
    @Timed
    @PublicAPI
    public Stream<Import> listSupportedImports() {
        return locationsService
                .getAvailableLocations()
                .stream() //
                .filter(l -> enabledImports.contains(l.getLocationType())) //
                .filter(DataSetLocation::isEnabled) //
                .map(l -> { //
                    final boolean defaultImport = LocalStoreLocation.NAME.equals(l.getLocationType());
                    ImportBuilder builder = ImportBuilder //
                            .builder() //
                            .locationType(l.getLocationType()) //
                            .contentType(l.getAcceptedContentType()) //
                            .defaultImport(defaultImport) //
                            .label(l.getLabel()) //
                            .title(l.getTitle());
                    if (l.isDynamic()) {
                        builder = builder.dynamic(true);
                    } else {
                        builder = builder.dynamic(false).parameters(l.getParameters(getLocale()));
                    }
                    return builder.build();
                }) //
                .sorted((i1, i2) -> { //
                    int i1Value = i1.isDefaultImport() ? 1 : -1;
                    int i2Value = i2.isDefaultImport() ? 1 : -1;
                    final int compare = i2Value - i1Value;
                    if (compare == 0) {
                        // Same level, use location type alphabetical order to determine order.
                        return i1.getLocationType().compareTo(i2.getLocationType());
                    } else {
                        return compare;
                    }
                });
    }

    /**
     * Return the semantic types for a given dataset / column.
     *
     * @param datasetId the datasetId id.
     * @param columnId the column id.
     * @return the semantic types for a given dataset / column.
     */
    @RequestMapping(value = "/datasets/{datasetId}/columns/{columnId}/types", method = GET)
    @ApiOperation(value = "list the types of the wanted column",
            notes = "This list can be used by user to change the column type.")
    @Timed
    @PublicAPI
    public List<SemanticDomain> getDataSetColumnSemanticCategories(
            @ApiParam(value = "The dataset id") @PathVariable String datasetId,
            @ApiParam(value = "The column id") @PathVariable String columnId) {

        LOG.debug("listing semantic categories for dataset #{} column #{}", datasetId, columnId);

        final DataSetMetadata metadata = dataSetMetadataRepository.get(datasetId);
        if (metadata == null) {
            throw new TDPException(DataSetErrorCodes.DATASET_DOES_NOT_EXIST,
                    ExceptionContext.withBuilder().put("id", datasetId).build());
        } else {
            try (final Stream<DataSetRow> records = contentStore.stream(metadata)) {

                final ColumnMetadata columnMetadata = metadata.getRowMetadata().getById(columnId);
                final Analyzer<Analyzers.Result> analyzer = analyzerService.build(columnMetadata, SEMANTIC);

                analyzer.init();
                records.map(r -> r.get(columnId)).forEach(analyzer::analyze);
                analyzer.end();

                final List<Analyzers.Result> analyzerResult = analyzer.getResult();
                final StatisticsAdapter statisticsAdapter = new StatisticsAdapter(40);
                statisticsAdapter.adapt(singletonList(columnMetadata), analyzerResult);
                LOG.debug("found {} for dataset #{}, column #{}", columnMetadata.getSemanticDomains(), datasetId,
                        columnId);
                return columnMetadata.getSemanticDomains();
            }
        }
    }

    /**
     * Verify validity of the supplied name for a data set. This check will fail if the supplied name is null or only
     * containing
     * whitespaces characters. It will also throw an exception if a quote is in the name as it is an illegal TQL chars
     * for searches.
     *
     * @param dataSetName the data set name to validate
     */
    private void checkDataSetName(String dataSetName) {
        if (dataSetName == null || dataSetName.contains("'")) {
            throw new TDPException(INVALID_DATASET_NAME,
                    ExceptionContext.withBuilder().put("name", dataSetName).build());
        }
    }

    /**
     * @return What is the maximum dataset size allowed.
     */
    private long getMaxDataSetSizeAllowed() {
        final long availableSpace = quotaService.getAvailableSpace();
        return maximumInputStreamSize > availableSpace ? availableSpace : maximumInputStreamSize;
    }

    /**
     * Return the list of DataSetMetadata corresponding to the search
     *
     * @param sort sort filter
     * @param order order filter
     * @param name name filter
     * @param nameStrict is it a strict search
     * @param certified certified filter
     * @param favorite favorite filter
     * @param limit limit number of result
     * @param favoritesIds list of favorties ids of the user
     * @return the list of DataSetMetadata corresponding to the search
     */
    private Stream<DataSetMetadata> findDataset(Sort sort, Order order, String name, boolean nameStrict,
            boolean certified, boolean favorite, boolean limit, Set<String> favoritesIds) {
        // Build filter for data sets
        final List<String> predicates = new ArrayList<>();
        predicates.add("lifecycle.importing = false");
        if (favorite) {
            if (favoritesIds != null && !favoritesIds.isEmpty()) {
                predicates.add("id in ["
                        + favoritesIds.stream().map(ds -> '\'' + ds + '\'').collect(Collectors.joining(",")) + "]");
            } else {
                // Wants favorites but user has no favorite
                return Stream.empty();
            }
        }
        if (certified) {
            predicates.add("governance.certificationStep = '" + Certification.CERTIFIED + "'");
        }

        if (StringUtils.isNotEmpty(name)) {
            final String regex = "(?i)" + Pattern.quote(name);
            final String filter;
            if (nameStrict) {
                filter = "name ~ '^" + regex + "$'";
            } else {
                filter = "name ~ '.*" + regex + ".*'";
            }
            predicates.add(filter);
        }

        final String tqlFilter = String.join(" and ", predicates);
        LOG.debug("TQL Filter in use: {}", tqlFilter);

        // Get all data sets according to filter
        Stream<DataSetMetadata> datasetList = dataSetMetadataRepository.list(tqlFilter, sort, order);

        if (sort == Sort.AUTHOR || sort == Sort.NAME) { // As theses are not well handled by mongo repository
            datasetList = datasetList.sorted(getDataSetMetadataComparator(sort, order));
        }

        return datasetList.limit(limit ? datasetListLimit : Long.MAX_VALUE);
    }
}
