// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.talend.daikon.exception.ExceptionContext.build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.api.EnrichedPreparation;
import org.talend.dataprep.api.service.command.common.HttpResponse;
import org.talend.dataprep.api.service.command.folder.*;
import org.talend.dataprep.api.service.command.preparation.PreparationListByFolder;
import org.talend.dataprep.command.CommandHelper;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.preparation.service.UserPreparation;
import org.talend.dataprep.security.SecurityProxy;

import com.fasterxml.jackson.core.JsonGenerator;
import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import reactor.core.Cancellation;
import reactor.core.publisher.Flux;

@RestController
public class FolderAPI extends APIService {

    @Autowired
    private DataSetMetadataBuilder metadataBuilder;

    /** Security proxy let the current thread to borrow another identity for a while. */
    @Autowired
    private SecurityProxy securityProxy;

    @RequestMapping(value = "/api/folders", method = GET)
    @ApiOperation(value = "List children folders of the parameter if null list root children.", produces = APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<StreamingResponseBody> children(@RequestParam(required = false) String parentId) {
        try {
            final GenericCommand<InputStream> foldersList = getCommand(FolderChildrenList.class, parentId);
            return CommandHelper.toStreaming(foldersList);
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDERS, e);
        }
    }

    @RequestMapping(value = "/api/folders/tree", method = GET)
    @ApiOperation(value = "List all folders", produces = APPLICATION_JSON_VALUE)
    @Timed
    public StreamingResponseBody getTree() {
        try {
            final HystrixCommand<InputStream> foldersList = getCommand(FolderTree.class);
            return CommandHelper.toStreaming(foldersList);
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDERS, e);
        }
    }

    @RequestMapping(value = "/api/folders/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get folder by id", produces = APPLICATION_JSON_VALUE, notes = "Get a folder by id")
    @Timed
    public StreamingResponseBody getFolderAndHierarchyById(@PathVariable(value = "id") final String id) {
        try {
            final HystrixCommand<InputStream> foldersList = getCommand(GetFolder.class, id);
            return CommandHelper.toStreaming(foldersList);
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_GET_FOLDERS, e);
        }
    }

    @RequestMapping(value = "/api/folders", method = PUT)
    @ApiOperation(value = "Add a folder.", produces = APPLICATION_JSON_VALUE)
    @Timed
    public StreamingResponseBody addFolder(@RequestParam final String parentId, @RequestParam final String path) {
        try {
            final HystrixCommand<InputStream> createChildFolder = getCommand(CreateChildFolder.class, parentId, path);
            return CommandHelper.toStreaming(createChildFolder);
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_CREATE_FOLDER, e);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     */
    @RequestMapping(value = "/api/folders/{id}", method = DELETE)
    @ApiOperation(value = "Remove a Folder")
    @Timed
    public ResponseEntity<Void> removeFolder(@PathVariable final String id, final OutputStream output) {
        try {
            final GenericCommand<HttpResponse> removeFolder = getCommand(RemoveFolder.class, id);
            return CommandHelper.async(removeFolder);
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_DELETE_FOLDER, e);
        }
    }

    @RequestMapping(value = "/api/folders/{id}/name", method = PUT)
    @ApiOperation(value = "Rename a Folder")
    @Timed
    public void renameFolder(@PathVariable final String id, @RequestBody final String newName) {

        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(newName)) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_RENAME_FOLDER);
        }

        try {
            final HystrixCommand<Void> renameFolder = getCommand(RenameFolder.class, id, newName);
            renameFolder.execute();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_RENAME_FOLDER, e);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     *
     * @param name The folder to search.
     * @param strict Strict mode means searched name is the full name.
     * @return the list of folders that match the given name.
     */
    @RequestMapping(value = "/api/folders/search", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Search Folders with parameter as part of the name", produces = APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<StreamingResponseBody> search(@RequestParam final String name, @RequestParam(required = false) final boolean strict) {
        try {
            final GenericCommand<InputStream> searchFolders = getCommand(SearchFolders.class, name, strict);
            return CommandHelper.toStreaming(searchFolders);
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDERS, e);
        }
    }

    /**
     * List all the folders and preparations out of the given id.
     *
     * @param id Where to list folders and preparations.
     */
    //@formatter:off
    @RequestMapping(value = "/api/folders/{id}/preparations", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all preparations for a given id.", notes = "Returns the list of preparations for the given id the current user is allowed to see.")
    @Timed
    public StreamingResponseBody listPreparationsByFolder(
            @PathVariable @ApiParam(name = "id", value = "The destination to search preparations from.") final String id, //
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "DATE") final String sort, //
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "DESC") final String order) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing preparations in destination {} (pool: {} )...", id, getConnectionStats());
        }

        return output -> {
            try (final JsonGenerator generator = mapper.getFactory().createGenerator(output)) {
                generator.writeStartObject();
                // Folder list
                final FolderChildrenList commandListFolders = getCommand(FolderChildrenList.class, id, sort, order);
                final Flux<Folder> folders = Flux.from(CommandHelper.toPublisher(Folder.class, mapper, commandListFolders));
                writeFluxToJsonArray(folders, "folders", generator);
                // Preparation list
                final PreparationListByFolder listPreparations = getCommand(PreparationListByFolder.class, id, sort, order);
                final Queue<DataSetMetadata> dataSetMetadata = new ArrayDeque<>();
                final Publisher<DataSetMetadata> dataSetMetadataPublisher = Flux.fromIterable(dataSetMetadata);

                final Flux<EnrichedPreparation> preparations = Flux
                        .from(CommandHelper.toPublisher(UserPreparation.class, mapper, listPreparations)) // From preparation list
                        .doOnNext(preparation -> {
                            if (preparation.getDataSetId() == null) {
                                dataSetMetadata.offer(null); // No data set metadata to get from preparation.
                            } else {
                                // get the dataset metadata
                                try {
                                    securityProxy.asTechnicalUser(); // because dataset are not shared
                                    dataSetMetadata.offer(getCommand(DataSetGetMetadata.class, preparation.getDataSetId()).execute());
                                } catch (Exception e) {
                                    dataSetMetadata.offer(null);
                                    LOG.debug("error reading dataset metadata {} : {}", preparation.getId(), e);
                                } finally {
                                    securityProxy.releaseIdentity();
                                }
                            }
                        })
                        .zipWith(dataSetMetadataPublisher, EnrichedPreparation::new); // Zip preparations and discovered metadata
                writeFluxToJsonArray(preparations, "preparations", generator);
                generator.writeEndObject();
            } catch (IOException e) {
                throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDER_ENTRIES, e, build().put("destination", id));
            }
        };
    }

    private static <T> Cancellation writeFluxToJsonArray(Flux<T> flux, String arrayElement, JsonGenerator generator) {
        return flux.doOnSubscribe(subscription -> {
            try {
                generator.writeArrayFieldStart(arrayElement);
            } catch (IOException e) {
                LOG.error("Unable to write content.", e);
            }
        }).doOnComplete(() -> {
            try {
                generator.writeEndArray();
            } catch (IOException e) {
                LOG.error("Unable to write content.", e);
            }
        }).subscribe(o -> {
            try {
                generator.writeObject(o);
            } catch (IOException e) {
                LOG.error("Unable to write content.", e);
            }
        });
    }
}
