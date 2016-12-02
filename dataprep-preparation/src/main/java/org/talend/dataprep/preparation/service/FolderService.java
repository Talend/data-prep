// ============================================================================
//
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

package org.talend.dataprep.preparation.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;
import static org.talend.dataprep.exception.error.FolderErrorCodes.FOLDER_NOT_EMPTY;
import static org.talend.dataprep.exception.error.FolderErrorCodes.FOLDER_NOT_FOUND;
import static org.talend.dataprep.util.SortAndOrderHelper.getFolderComparator;

import java.util.List;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderInfo;
import org.talend.dataprep.api.folder.FolderTreeNode;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.folder.store.NotEmptyFolderException;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.Security;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(value = "folders", basePath = "/folders", description = "Operations on folders")
public class FolderService {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderService.class);

    /** Where the folders are stored. */
    @Autowired
    private FolderRepository folderRepository;

    /** DataPrep abstraction to the underlying security (whether it's enabled or not). */
    @Autowired
    private Security security;

    /**
     * List direct sub folders for the given id.
     *
     * @param id the current folder where to look for children.
     * @return direct sub folders for the given id.
     */
    //@formatter:off
    @RequestMapping(value = "/folders/{id}/children", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Folder children", produces = APPLICATION_JSON_VALUE, notes = "List all child folders of the one as parameter")
    @Timed
    public Iterable<Folder> children(@PathVariable String id,
                                     @RequestParam(defaultValue = "MODIF") @ApiParam(value = "Sort key (by name or date).") String sort,
                                     @RequestParam(defaultValue = "DESC") @ApiParam(value = "Order for sort key (desc or asc).") String order) {
    //@formatter:on

        if (!folderRepository.exists(id)) {
            throw new TDPException(FOLDER_NOT_FOUND, build().put("id", id));
        }

        Iterable<Folder> children = folderRepository.children(id);

        // update the number of preparations in each children
        children.forEach(f -> {
            final long count = stream(folderRepository.entries(f.getId(), PREPARATION).spliterator(), false).count();
            f.setNbPreparations(count);
        });

        // sort the folders
        children = StreamSupport.stream(children.spliterator(), false) //
                .sorted(getFolderComparator(sort, order)) //
                .collect(toList());

        LOGGER.info("found {} children for {}", stream(children.spliterator(), false).count(), id);
        return children;
    }

    /**
     * Get a folder metadata with its hierarchy
     *
     * @param id the folder id.
     * @return the folder metadata with its hierarchy.
     */
    @RequestMapping(value = "/folders/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get folder by id", produces = APPLICATION_JSON_VALUE, notes = "GET a folder by id")
    @Timed
    public FolderInfo getFolderAndHierarchyById(@PathVariable(value = "id") final String id) {
        final Folder folder = folderRepository.getFolderById(id);
        final List<Folder> hierarchy = folderRepository.getHierarchy(folder);

        return new FolderInfo(folder, hierarchy);
    }

    /**
     * Search for folders.
     *
     * @param name the folder name to search.
     * @param strict strict mode means the name is the full name.
     * @return the folders whose part of their name match the given path.
     */
    @RequestMapping(value = "/folders/search", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Search Folders with parameter as part of the name", produces = APPLICATION_JSON_VALUE)
    @Timed
    public Iterable<Folder> search(@RequestParam final String name,
                                   @RequestParam(required = false) final boolean strict) {
        final Iterable<Folder> folders = folderRepository.searchFolders(name, strict);

        int foldersFound = 0;
        for (Folder folder : folders) {
            final long count = stream(folderRepository.entries(folder.getId(), PREPARATION).spliterator(), false).count();
            folder.setNbPreparations(count);
            foldersFound++;
        }

        LOGGER.info("Found {} folder(s) searching for {}", foldersFound, name);

        return folders;
    }

    /**
     * Add a folder.
     *
     * @param parentId where to add the folder.
     * @return the created folder.
     */
    @RequestMapping(value = "/folders", method = PUT, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create a Folder", produces = APPLICATION_JSON_VALUE, notes = "Create a folder")
    @Timed
    public Folder addFolder(@RequestParam String parentId, @RequestParam String path) {
        return folderRepository.addFolder(parentId, path);
    }

    /**
     * Remove the folder. Throws an exception if the folder, or one of its sub folders, contains an entry.
     *
     * @param id the id that points to the folder to remove.
     */
    @RequestMapping(value = "/folders/{id}", method = DELETE)
    @ApiOperation(value = "Remove a Folder", produces = APPLICATION_JSON_VALUE, notes = "Remove the folder")
    @Timed
    public void removeFolder(@PathVariable String id) {
        try {
            folderRepository.removeFolder(id);
        } catch (NotEmptyFolderException e) {
            throw new TDPException(FOLDER_NOT_EMPTY, e);
        }
    }

    /**
     * Rename the folder to the new id.
     *
     * @param id where to look for the folder.
     * @param newName the new folder id.
     */
    @RequestMapping(value = "/folders/{id}/name", method = PUT)
    @ApiOperation(value = "Rename a Folder", produces = APPLICATION_JSON_VALUE)
    @Timed
    public void renameFolder(@PathVariable String id, @RequestBody String newName) {
        folderRepository.renameFolder(id, newName);
    }

    @RequestMapping(value = "/folders/tree", method = GET)
    @ApiOperation(value = "List all folders", produces = APPLICATION_JSON_VALUE)
    @Timed
    public FolderTreeNode getTree() {
        final Folder home = folderRepository.getHome();
        return getTree(home);
    }

    private FolderTreeNode getTree(final Folder root) {
        final Iterable<Folder> children = folderRepository.children(root.getId());
        final List<FolderTreeNode> childrenSubtrees = StreamSupport.stream(children.spliterator(), false)
                .map(this::getTree)
                .collect(toList());
        return new FolderTreeNode(root, childrenSubtrees);
    }
}
