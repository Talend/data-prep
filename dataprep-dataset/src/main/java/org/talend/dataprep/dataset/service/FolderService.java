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
    @ApiOperation(value = "Create a Folder", produces = MediaType.APPLICATION_JSON_VALUE, notes = "Create a folder in the one with the id, If null create as a child root")
    @Timed
    @VolumeMetered
    public Folder addFolder(@RequestParam(required = false) String parentPath, @RequestParam(required = true) String path){

        Folder folder = folderRepository.addFolder(parentPath == null ? "" : parentPath, path);
        return folder;
    }



}
