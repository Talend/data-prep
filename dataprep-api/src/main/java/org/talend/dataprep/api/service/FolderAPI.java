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

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.command.CommandHelper.toStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.preparation.PreparationListItemDTO;
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
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;

import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
public class FolderAPI extends APIService {

    private static final String FOLDER_ID = "folderId";

    @GetMapping(value = "/api/folders")
    @ApiOperation(value = "List folders. Optional filter on parent ID may be supplied.",
            produces = APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<StreamingResponseBody> listFolders(@RequestParam(required = false) String parentId) {
        try {
            final GenericCommand<InputStream> foldersList = getCommand(FolderChildrenList.class, parentId);
            return CommandHelper.toStreaming(foldersList);
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDERS, e);
        }
    }

    @GetMapping(value = "/api/folders/tree")
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

    @GetMapping(value = "/api/folders/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get folder by id", produces = APPLICATION_JSON_VALUE, notes = "Get a folder by id")
    @Timed
    public ResponseEntity<StreamingResponseBody>
            getFolderAndHierarchyById(@PathVariable(value = "id") final String id) {
        try {
            final HystrixCommand<InputStream> foldersList = getCommand(GetFolder.class, id);
            return ResponseEntity
                    .ok() //
                    .contentType(APPLICATION_JSON_UTF8) //
                    .body(CommandHelper.toStreaming(foldersList));
        } catch (Exception e) {
            final ExceptionContext context = ExceptionContext.build().put(FOLDER_ID, id);
            throw new TDPException(APIErrorCodes.UNABLE_TO_GET_FOLDERS, e, context);
        }
    }

    @PutMapping(value = "/api/folders")
    @ApiOperation(value = "Add a folder.", produces = APPLICATION_JSON_VALUE)
    @Timed
    public StreamingResponseBody addFolder(@RequestParam(required = false) final String parentId,
            @RequestParam final String path) {
        try {
            final HystrixCommand<InputStream> createChildFolder = getCommand(CreateChildFolder.class, parentId, path);
            return CommandHelper.toStreaming(createChildFolder);
        } catch (Exception e) {
            final ExceptionContext context = ExceptionContext.build().put(FOLDER_ID, parentId).put("path", path);
            throw new TDPException(APIErrorCodes.UNABLE_TO_CREATE_FOLDER, e, context);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     */
    @DeleteMapping(value = "/api/folders/{id}")
    @ApiOperation(value = "Remove a Folder")
    @Timed
    public ResponseEntity<String> removeFolder(@PathVariable final String id, final OutputStream output) {
        try {
            return getCommand(RemoveFolder.class, id).execute();
        } catch (Exception e) {
            final ExceptionContext context = ExceptionContext.build().put(FOLDER_ID, id);
            throw new TDPException(APIErrorCodes.UNABLE_TO_DELETE_FOLDER, e, context);
        }
    }

    @PutMapping(value = "/api/folders/{id}/name")
    @ApiOperation(value = "Rename a Folder")
    @Timed
    public void renameFolder(@PathVariable final String id, @RequestBody final String newName) {

        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(newName)) {
            final ExceptionContext context = ExceptionContext.build().put(FOLDER_ID, id);
            throw new TDPException(APIErrorCodes.UNABLE_TO_RENAME_FOLDER, context);
        }

        try {
            final HystrixCommand<Void> renameFolder = getCommand(RenameFolder.class, id, newName);
            renameFolder.execute();
        } catch (Exception e) {
            final ExceptionContext context = ExceptionContext.build().put(PREPARATION_ID, id);
            throw new TDPException(APIErrorCodes.UNABLE_TO_RENAME_FOLDER, e, context);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     *
     * @param folderName The folder to search.
     * @param strict Strict mode means searched name is the full name.
     * @return the list of folders that match the given name.
     */
    @GetMapping(value = "/api/folders/search", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Search Folders with parameter as part of the name", produces = APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<StreamingResponseBody> search(@RequestParam(required = false) final String name,
            @RequestParam(required = false) final Boolean strict, @RequestParam(required = false) final String path) {
        try {
            final GenericCommand<InputStream> searchFolders = getCommand(SearchFolders.class, name, strict, path);
            return CommandHelper.toStreaming(searchFolders);
        } catch (Exception e) {
            final ExceptionContext context =
                    ExceptionContext.build().put("folderName", name).put("strict", strict).put("path", path);
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDERS, e, context);
        }
    }

    /**
     * List all the folders and preparations out of the given id.
     *
     * @param id Where to list folders and preparations.
     */
    //@formatter:off
    @GetMapping(value = "/api/folders/{id}/preparations", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all preparations for a given id.", notes = "Returns the list of preparations for the given id the current user is allowed to see.")
    @Timed
    public PreparationsByFolder listPreparationsByFolder(
            @PathVariable @ApiParam(name = "id", value = "The destination to search preparations from.") final String id, //
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "creationDate") final Sort sort, //
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "desc") final Order order) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing preparations in destination {} (pool: {} )...", id, getConnectionStats());
        }

        LOG.info("Listing preparations in folder {}", id);

        final FolderChildrenList commandListFolders = getCommand(FolderChildrenList.class, id, sort, order);
        final Stream<Folder> folders = toStream(Folder.class, mapper, commandListFolders);

        final PreparationListByFolder listPreparations = getCommand(PreparationListByFolder.class, id, sort, order);
        final Stream<PreparationListItemDTO> preparations = toStream(PreparationDTO.class, mapper, listPreparations) //
                .map(dto -> beanConversionService.convert(dto, PreparationListItemDTO.class,
                        APIService::injectDataSetName));

        return new PreparationsByFolder(folders, preparations);
    }

    public static class PreparationsByFolder {

        private final Stream<Folder> folders;

        private final Stream<PreparationListItemDTO> preparations;

        public PreparationsByFolder(Stream<Folder> folders, Stream<PreparationListItemDTO> preparations) {
            this.folders = folders;
            this.preparations = preparations;
        }

        public Stream<Folder> getFolders() {
            return folders;
        }

        public Stream<PreparationListItemDTO> getPreparations() {
            return preparations;
        }
    }

}
