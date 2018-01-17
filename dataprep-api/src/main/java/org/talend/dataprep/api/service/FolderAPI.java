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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.command.CommandHelper.toPublisher;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.service.api.EnrichedPreparation;
import org.talend.dataprep.api.service.command.folder.CreateChildFolder;
import org.talend.dataprep.api.service.command.folder.FolderChildrenList;
import org.talend.dataprep.api.service.command.folder.FolderTree;
import org.talend.dataprep.api.service.command.folder.GetFolder;
import org.talend.dataprep.api.service.command.folder.RemoveFolder;
import org.talend.dataprep.api.service.command.folder.RenameFolder;
import org.talend.dataprep.api.service.command.folder.SearchFolders;
import org.talend.dataprep.command.CommandHelper;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.command.preparation.PreparationListByFolder;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.preparation.service.UserPreparation;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;

import com.fasterxml.jackson.core.JsonGenerator;
import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import reactor.core.publisher.Flux;

@RestController
public class FolderAPI extends APIService {

    @Autowired
    private DataSetMetadataBuilder metadataBuilder;

    /** Security proxy let the current thread to borrow another identity for a while. */
    @Autowired
    private SecurityProxy securityProxy;

    @RequestMapping(value = "/api/folders", method = GET)
    @ApiOperation(value = "List folders. Optional filter on parent ID may be supplied.", produces = APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<StreamingResponseBody> listFolders(@RequestParam(required = false) String parentId) {
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
    public StreamingResponseBody addFolder(@RequestParam(required = false) final String parentId, @RequestParam final String path) {
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
    public ResponseEntity<String> removeFolder(@PathVariable final String id, final OutputStream output) {
        try {
            return getCommand(RemoveFolder.class, id).execute();
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
    public ResponseEntity<StreamingResponseBody> search(@RequestParam(required = false) final String name,
                                                        @RequestParam(required = false) final Boolean strict,
                                                        @RequestParam(required = false) final String path) {
        try {
            final GenericCommand<InputStream> searchFolders = getCommand(SearchFolders.class, name, strict, path);
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
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "creationDate") final Sort sort, //
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "desc") final Order order) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing preparations in destination {} (pool: {} )...", id, getConnectionStats());
        }

        LOG.info("Listing preparations in folder {}", id);

        return output -> {
            try (final JsonGenerator generator = mapper.getFactory().createGenerator(output)) {
                generator.writeStartObject();
                // Folder list
                final FolderChildrenList commandListFolders = getCommand(FolderChildrenList.class, id, sort, order);
                final Flux<Folder> folders = Flux.from(toPublisher(Folder.class, mapper, commandListFolders));
                writeFluxToJsonArray(folders, "folders", generator);
                // Preparation list
                final PreparationListByFolder listPreparations = getCommand(PreparationListByFolder.class, id, sort, order);

                final Flux<EnrichedPreparation> preparations = Flux
                        .from(toPublisher(UserPreparation.class, mapper, listPreparations)) // From preparation list
                        .map(preparation -> beanConversionService.convert(preparation, EnrichedPreparation.class));
                writeFluxToJsonArray(preparations, "preparations", generator);
                generator.writeEndObject();
            } catch (EOFException e) {
                LOG.debug("Output stream has been closed before finishing preparation writing.", e);
            } catch (IOException e) {
                throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDER_ENTRIES, e, build().put("destination", id));
            }
        };
    }

    private static <T> void writeFluxToJsonArray(Flux<T> flux, String arrayElement, JsonGenerator generator) {
        flux.subscribe(new WriteJsonArraySubscriber<>(generator, arrayElement));
    }

    private static class WriteJsonArraySubscriber<T> implements Subscriber<T> {

        private final JsonGenerator generator;

        private final String arrayElement;

        private Subscription subscription;

        public WriteJsonArraySubscriber(JsonGenerator generator, String arrayElement) {
            this.generator = generator;
            this.arrayElement = arrayElement;
        }

        @Override
        public void onSubscribe(Subscription s) {
            try {
                generator.writeArrayFieldStart(arrayElement);
            } catch (EOFException eofe) {
                LOG.debug("JsonGenerator was closed before finish streaming.", eofe);
                subscription.cancel();
            } catch (IOException e) {
                LOG.error("Unable to write content.", e);
            }
            subscription = s;
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(T aLong) {
            try {
                generator.writeObject(aLong);
            } catch (EOFException eofe) {
                LOG.debug("JsonGenerator was closed before finish streaming.", eofe);
                subscription.cancel();
            } catch (IOException e) {
                LOG.error("Unable to write content.", e);
            }
        }

        @Override
        public void onError(Throwable t) {
            onComplete();
        }

        @Override
        public void onComplete() {
            try {
                generator.writeEndArray();
            } catch (EOFException eofe) {
                LOG.debug("JsonGenerator was closed before finish streaming.", eofe);
            } catch (IOException e) {
                LOG.error("Unable to write content.", e);
            }
        }
    }
}
