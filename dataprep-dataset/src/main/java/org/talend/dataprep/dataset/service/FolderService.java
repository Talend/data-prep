package org.talend.dataprep.dataset.service;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import javax.inject.Inject;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.folder.store.NotEmptyFolderException;
import org.talend.dataprep.metrics.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value = "folders", basePath = "/folders", description = "Operations on folders")
public class FolderService {

    private FolderRepository folderRepository;

    @Inject
    public FolderService( FolderRepository folderRepository ) {
        this.folderRepository = folderRepository;
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param path
     * @return
     */
    @RequestMapping(value = "/folders", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Folder children", produces = MediaType.APPLICATION_JSON_VALUE, notes = "List all child folders of the one as parameter")
    @Timed
    public Iterable<Folder> children( @RequestParam(required = false)  String path){
        return folderRepository.children( path == null ? "" : path);
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @return
     */
    @RequestMapping(value = "/folders/all", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "All Folders", produces = MediaType.APPLICATION_JSON_VALUE, notes = "List all existing folders")
    @Timed
    public Iterable<Folder> allFolder( ){
        return folderRepository.allFolder();
    }


    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param pathName
     * @return
     */
    @RequestMapping(value = "/folders/search", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Search Folders with parameter as part of the name", produces = MediaType.APPLICATION_JSON_VALUE, notes = "")
    @Timed
    public Iterable<Folder> search( @RequestParam(required = false)  String pathName){
        return folderRepository.searchFolders(pathName);
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param path
     * @return
     */
    @RequestMapping(value = "/folders", method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create a Folder", produces = MediaType.APPLICATION_JSON_VALUE, notes = "Create a folder")
    @Timed
    public Folder addFolder(@RequestParam String path){
        Folder folder = folderRepository.addFolder(path);
        return folder;
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param path
     * @return
     */
    @RequestMapping(value = "/folders", method = DELETE)
    @ApiOperation(value = "Remove a Folder", produces = MediaType.APPLICATION_JSON_VALUE, notes = "Remove the folder")
    @Timed
    public void removeFolder(@RequestParam String path) {
        try {
            folderRepository.removeFolder(path);
        } catch (NotEmptyFolderException e) {
            throw new TDPException(DataSetErrorCodes.FOLDER_NOT_EMPTY);
        }
    }


    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param path
     * @param newPath
     */
    @RequestMapping(value = "/folders/rename", method = PUT)
    @ApiOperation(value = "Rename a Folder", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void renameFolder(@RequestParam String path, @RequestParam String newPath){
        folderRepository.renameFolder( path, newPath );
    }


    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param folderEntry
     * @return
     */
    @RequestMapping(value = "/folders/entries", method = PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes =  MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add a FolderEntry", produces = MediaType.APPLICATION_JSON_VALUE, notes = "Add the folder entry")
    @Timed
    public FolderEntry addFolderEntry(@RequestBody FolderEntry folderEntry){
        // jackson/json deserialization use empty constructor so we need to ensure id is build
        folderEntry.buildId();
        return folderRepository.addFolderEntry( folderEntry );
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param contentId
     * @param contentType
     * @return
     */
    @RequestMapping(value = "/folders/entries/{contentType}/{id}", method = DELETE)
    @ApiOperation(value = "Remove a FolderEntry", notes = "Delete the folder entry")
    @Timed
    public void deleteFolderEntry(@RequestParam String path, @PathVariable(value = "id") String contentId, //
                                  @PathVariable(value = "contentType") String contentType){

        folderRepository.removeFolderEntry( path, contentId, contentType );
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param path
     * @param contentType
     * @return
     */
    @RequestMapping(value = "/folders/entries", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List folder entries", produces = MediaType.APPLICATION_JSON_VALUE, notes = "List all folder entries of the given content type")
    @Timed
    public Iterable<FolderEntry> entries(@RequestParam String path, @RequestParam String contentType){
        return folderRepository.entries( path, contentType );
    }



}
