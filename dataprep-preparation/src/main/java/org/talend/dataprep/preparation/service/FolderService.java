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

package org.talend.dataprep.preparation.service;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;
import static org.talend.dataprep.exception.error.FolderErrorCodes.FOLDER_NOT_FOUND;
import static org.talend.dataprep.util.SortAndOrderHelper.getFolderComparator;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderInfo;
import org.talend.dataprep.api.folder.FolderTreeNode;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;

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
     * Get folders. If parentId is supplied, it will be used as filter.
     *
     * @param parentId the parent folder id parameter
     * @return direct sub folders for the given id.
     */
    //@formatter:off
    @RequestMapping(value = "/folders", method = GET)
    @ApiOperation(value = "List children folders of the parameter if null list root children.", notes = "List all child folders of the one as parameter")
    @Timed
    public Stream<Folder> list(@RequestParam(required = false) @ApiParam(value = "Parent id filter.") String parentId,
                                   @RequestParam(defaultValue = "lastModificationDate") @ApiParam(value = "Sort key (by name or date).") Sort sort,
                                   @RequestParam(defaultValue = "desc") @ApiParam(value = "Order for sort key (desc or asc).") Order order) {
    //@formatter:on

        Stream<Folder> children;
        if (parentId != null) {
            if (!folderRepository.exists(parentId)) {
                throw new TDPException(FOLDER_NOT_FOUND, build().put("id", parentId));
            }
            children = folderRepository.children(parentId);
        } else {
            // This will list all folders
            children = folderRepository.searchFolders("", false);
        }

        final AtomicInteger folderCount = new AtomicInteger();

        // update the number of preparations in each children
        children = children.peek(f -> {
            final long count = folderRepository.count(f.getId(), PREPARATION);
            f.setNbPreparations(count);
            folderCount.addAndGet(1);
        });

        LOGGER.info("Found {} children for parentId: {}", folderCount.get(), parentId);

        // sort the folders
        return children.sorted(getFolderComparator(sort, order));
    }

    /**
     * Get a folder metadata with its hierarchy
     *
     * @param id the folder id.
     * @return the folder metadata with its hierarchy.
     */
    @RequestMapping(value = "/folders/{id}", method = GET)
    @ApiOperation(value = "Get folder by id", notes = "GET a folder by id")
    @Timed
    public FolderInfo getFolderAndHierarchyById(@PathVariable(value = "id") final String id) {
        final Folder folder = folderRepository.getFolderById(id);
        if (folder == null) {
            throw new TDPException(FOLDER_NOT_FOUND, ExceptionContext.build().put("path", id));
        }
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
    @RequestMapping(value = "/folders/search", method = GET)
    @ApiOperation(value = "Search Folders with parameter as part of the name")
    @Timed
    public Stream<Folder> search(@RequestParam(required = false, defaultValue = "") final String name,
                                   @RequestParam(required = false, defaultValue = "false") final Boolean strict,
                                   @RequestParam(required = false) final String path) {
        Stream<Folder> folders;
        if (path == null) {
            folders = folderRepository.searchFolders(name, strict);
        } else {
            folders = folderRepository.searchFolders(name, strict).filter(f -> f.getPath().equals(path));
        }

        AtomicInteger foldersFound = new AtomicInteger(0);
        folders = folders.peek(folder -> {
            folder.setNbPreparations(folderRepository.count(folder.getId(), PREPARATION));
            foldersFound.incrementAndGet();
        });

        LOGGER.info("Found {} folder(s) searching for {}", foldersFound, name);

        return folders;
    }

    /**
     * Add a folder.
     *
     * @param parentId where to add the folder.
     * @return the created folder.
     */
    @RequestMapping(value = "/folders", method = PUT)
    @ApiOperation(value = "Create a Folder", notes = "Create a folder")
    @Timed
    public Folder addFolder(@RequestParam(required = false) String parentId, @RequestParam String path) {
        if (parentId == null) {
            parentId = folderRepository.getHome().getId();
        }
        return folderRepository.addFolder(parentId, path);
    }

    /**
     * Remove the folder. Throws an exception if the folder, or one of its sub folders, contains an entry.
     *
     * @param id the id that points to the folder to remove.
     */
    @RequestMapping(value = "/folders/{id}", method = DELETE)
    @ApiOperation(value = "Remove a Folder", notes = "Remove the folder")
    @Timed
    public void removeFolder(@PathVariable String id) {
        folderRepository.removeFolder(id);
    }

    /**
     * Rename the folder to the new id.
     *
     * @param id where to look for the folder.
     * @param newName the new folder id.
     */
    @RequestMapping(value = "/folders/{id}/name", method = PUT)
    @ApiOperation(value = "Rename a Folder")
    @Timed
    public void renameFolder(@PathVariable String id, @RequestBody String newName) {
        folderRepository.renameFolder(id, newName);
    }

    @RequestMapping(value = "/folders/tree", method = GET)
    @ApiOperation(value = "List all folders")
    @Timed
    public FolderTreeNode getTree() {
        final Folder home = folderRepository.getHome();
        return getTree(home);
    }

    private FolderTreeNode getTree(final Folder root) {
        try (final Stream<Folder> children = folderRepository.children(root.getId())) {
            final List<FolderTreeNode> childrenSubtrees = StreamSupport.stream(children.spliterator(), false).map(this::getTree)
                    .collect(toList());
            return new FolderTreeNode(root, childrenSubtrees);
        }
    }
}
