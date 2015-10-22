package org.talend.dataprep.dataset.service;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.exception.error.FolderErrorCodes;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;

import javax.inject.Inject;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@Api(value = "folders", basePath = "/folders", description = "Operations on folders")
public class FolderService {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FolderService.class);

    private FolderRepository folderRepository;

    @Inject
    public FolderService( FolderRepository folderRepository ) {
        this.folderRepository = folderRepository;
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param folder
     * @return
     */
    @RequestMapping(value = "/folders/childs", method = POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Folder childs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, notes = "List all child folders of the one as parameter")
    @Timed
    @VolumeMetered
    public Iterable<Folder> childs( Folder folder){
        return folderRepository.childs(folder);
    }


    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param folder
     * @return
     */
    @RequestMapping(value = "/folders/root/childs", method = GET, consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Folder childs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            notes = "List all child folders of the root folder")
    @Timed
    @VolumeMetered
    public Iterable<Folder> rootChilds( Folder folder){
        return folderRepository.childs(folderRepository.rootFolder());
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param folder
     * @return
     */
    @RequestMapping(value = "/folders", method = PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create a Folder", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, notes = "Create a folder in the one with the id, If null create as a child root")
    @Timed
    @VolumeMetered
    public Folder addFolder(@RequestParam(required = false) String parentId, @RequestBody Folder folder){

        Folder parent;
        if (StringUtils.isEmpty(parentId)){
            parent = folderRepository.rootFolder();
        } else {
            parent = folderRepository.find(parentId);
        }
        if (parent == null){
            throw new TDPException(FolderErrorCodes.FOLDER_DOES_NOT_EXIST, ExceptionContext.build().put("folderId", parentId));
        }
        folder = folderRepository.addFolder(parent, folder);
        return folder;
    }



}
