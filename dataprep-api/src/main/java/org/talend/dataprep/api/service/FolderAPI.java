package org.talend.dataprep.api.service;

import com.netflix.hystrix.HystrixCommand;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.service.command.folder.CreateChildFolder;
import org.talend.dataprep.api.service.command.folder.CreateFolderEntry;
import org.talend.dataprep.api.service.command.folder.FoldersList;
import org.talend.dataprep.api.service.command.folder.RemoveFolder;
import org.talend.dataprep.api.service.command.folder.RemoveFolderEntry;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Api(value = "api", basePath = "/api", description = "Folders API")
public class FolderAPI extends APIService {


    @RequestMapping(value = "/api/folders/childs", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List childs folders of the parameter if null list root childs.")
    @Timed
    public void childs(@RequestParam(required = false)  String path, final HttpServletResponse response) {
        try {
            final HystrixCommand<InputStream> foldersList = getCommand(FoldersList.class, getClient(), path);
            response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            final ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(foldersList.execute(), outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDERS, e);
        }
    }


    @RequestMapping(value = "/api/folders/add", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add a folder.")
    @Timed
    public void addFolder(@RequestParam(required = true) String path, //
            final HttpServletResponse response) {
        try {
            final HystrixCommand<InputStream> createChildFolder = getCommand(CreateChildFolder.class, getClient(), path);
            response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            final ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(createChildFolder.execute(), outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_CREATE_FOLDER, e);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param path
     * @return
     */
    @RequestMapping(value = "/api/folders", method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Remove a Folder", produces = MediaType.APPLICATION_JSON_VALUE, notes = "Remove the folder")
    @Timed
    public void removeFolder(@RequestParam(required = true) String path){
        try {
            final HystrixCommand<Void> removeFolder = getCommand(RemoveFolder.class, getClient(), path);
            removeFolder.execute();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_DELETE_FOLDER, e);
        }
    }



    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param folderEntry
     * @return
     */
    @RequestMapping(value = "/api/folders/entries", method = PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add a FolderEntry", produces = MediaType.APPLICATION_JSON_VALUE, notes = "Add the folder entry")
    @Timed
    @VolumeMetered
    public void addFolderEntry(@RequestBody FolderEntry folderEntry){
        try {
            final HystrixCommand<Void> createFolderEntry = getCommand(CreateFolderEntry.class, getClient(), folderEntry);
            createFolderEntry.execute();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_CREATE_FOLDER_ENTRY, e);
        }
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
        try {
            final HystrixCommand<Void> createFolderEntry = getCommand(RemoveFolderEntry.class, getClient(), folderEntry);
            createFolderEntry.execute();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_DELETE_FOLDER_ENTRY, e);
        }
    }



    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param path
     * @param contentType
     * @return
     */
    @RequestMapping(value = "/folders/entries", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List Folder entries", produces = MediaType.APPLICATION_JSON_VALUE, notes = "List all folder entries of the given content type")
    @Timed
    @VolumeMetered
    public Iterable<FolderEntry> entries(@RequestParam String path, @RequestParam String contentType, final HttpServletResponse response){
        try {
            final HystrixCommand<InputStream> listFolderEntries = getCommand(ListFolderEntry.class, getClient(), folderEntry);
            response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            final ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(listFolderEntries.execute(), outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDER_ENTRIES, e);
        }
    }

}
