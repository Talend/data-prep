package org.talend.dataprep.dataset.service;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;

import javax.inject.Inject;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@Api(value = "folders", basePath = "/folders", description = "Operations on folders")
public class FolderService {

    private static final Logger LOG = LoggerFactory.getLogger(FolderService.class);

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
    @RequestMapping(value = "/folders/childs", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Folder childs", produces = MediaType.APPLICATION_JSON_VALUE, notes = "List all child folders of the one as parameter")
    @Timed
    @VolumeMetered
    public Iterable<Folder> childs( @RequestParam(required = false)  String path){
        return folderRepository.childs(path == null ? "" : path);
    }


    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param path
     * @return
     */
    @RequestMapping(value = "/folders/add", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create a Folder", produces = MediaType.APPLICATION_JSON_VALUE, notes = "Create a folder")
    @Timed
    @VolumeMetered
    public Folder addFolder(@RequestParam String path){

        Folder folder = folderRepository.addFolder(path);
        return folder;
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param path
     * @return
     */
    @RequestMapping(value = "/folders", method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Remove a Folder", produces = MediaType.APPLICATION_JSON_VALUE, notes = "Remove the folder")
    @Timed
    @VolumeMetered
    public void removeFolder(@RequestParam String path){
        folderRepository.removeFolder(path);
    }



    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param folderEntry
     * @return
     */
    @RequestMapping(value = "/folders/entries", method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add a FolderEntry", produces = MediaType.APPLICATION_JSON_VALUE, notes = "Add the folder entry")
    @Timed
    @VolumeMetered
    public void addFolderEntry(@RequestBody FolderEntry folderEntry){
        folderRepository.addFolderEntry( folderEntry );
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param folderEntry
     * @return
     */
    @RequestMapping(value = "/folders/entries", method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Remove a FolderEntry", produces = MediaType.APPLICATION_JSON_VALUE, notes = "Delete the folder entry")
    @Timed
    @VolumeMetered
    public void deleteFolderEntry(@RequestBody FolderEntry folderEntry){
        folderRepository.removeFolderEntry( folderEntry );
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
    @VolumeMetered
    public Iterable<FolderEntry> entries(@RequestParam String path, @RequestParam String contentType){
        return folderRepository.entries( path, contentType );
    }

}
