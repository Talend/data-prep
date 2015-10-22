package org.talend.dataprep.dataset.service;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;

import javax.inject.Inject;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
    @ApiOperation(value = "Create a data set", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, notes = "List all child folders of the one as parameter")
    @Timed
    @VolumeMetered
    public Iterable<Folder> childs( Folder folder){
        return folderRepository.childs(folder);
    }





}
