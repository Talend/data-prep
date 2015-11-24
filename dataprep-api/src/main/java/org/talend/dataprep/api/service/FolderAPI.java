package org.talend.dataprep.api.service;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.folder.FolderContent;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.service.command.folder.AllFoldersList;
import org.talend.dataprep.api.service.command.folder.CreateChildFolder;
import org.talend.dataprep.api.service.command.folder.CreateFolderEntry;
import org.talend.dataprep.api.service.command.folder.FolderDataSetList;
import org.talend.dataprep.api.service.command.folder.FolderEntriesList;
import org.talend.dataprep.api.service.command.folder.FoldersList;
import org.talend.dataprep.api.service.command.folder.RemoveFolder;
import org.talend.dataprep.api.service.command.folder.RemoveFolderEntry;
import org.talend.dataprep.api.service.command.folder.RenameFolder;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;

import com.netflix.hystrix.HystrixCommand;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@RestController
@Api(value = "api", basePath = "/api", description = "Folders API")
public class FolderAPI extends APIService {

    @RequestMapping(value = "/api/folders", method = GET)
    @ApiOperation(value = "List childs folders of the parameter if null list root childs.", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void childs(@RequestParam(required = false) String path, final HttpServletResponse response) {
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

    @RequestMapping(value = "/api/folders/all", method = GET)
    @ApiOperation(value = "List all folders.", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void allFolder(final HttpServletResponse response) {
        try {
            final HystrixCommand<InputStream> foldersList = getCommand(AllFoldersList.class, getClient());
            response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            final ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(foldersList.execute(), outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDERS, e);
        }
    }

    @RequestMapping(value = "/api/folders", method = PUT)
    @ApiOperation(value = "Add a folder.", produces = MediaType.APPLICATION_JSON_VALUE)
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
     * 
     * @param path
     * @return
     */
    @RequestMapping(value = "/api/folders", method = DELETE)
    @ApiOperation(value = "Remove a Folder")
    @Timed
    public void removeFolder(@RequestParam(required = true) String path) {
        try {
            final HystrixCommand<Void> removeFolder = getCommand(RemoveFolder.class, getClient(), path);
            removeFolder.execute();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_DELETE_FOLDER, e);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * @param path
     * @param newPath
     */
    @RequestMapping(value = "/api/folders/rename", method = PUT)
    @ApiOperation(value = "Rename a Folder")
    @Timed
    @VolumeMetered
    public void renameFolder(@RequestParam String path, @RequestParam String newPath){

        if ( StringUtils.isEmpty( path) //
            || StringUtils.isEmpty(newPath) //
            || StringUtils.containsOnly(path, "/")) {

            throw new TDPException(APIErrorCodes.UNABLE_TO_RENAME_FOLDER);
        }
        try {
            final HystrixCommand<Void> renameFolder = getCommand( RenameFolder.class, getClient(), path, newPath);
            renameFolder.execute();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_RENAME_FOLDER, e);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * 
     * @param folderEntry
     * @return
     */
    @RequestMapping(value = "/api/folders/entries", method = PUT, consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add a FolderEntry", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Timed
    @VolumeMetered
    public void addFolderEntry(@RequestBody FolderEntry folderEntry, final HttpServletResponse response) {
        try {
            final HystrixCommand<InputStream> createFolderEntry = getCommand(CreateFolderEntry.class, getClient(), folderEntry);
            response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            final ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(createFolderEntry.execute(), outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_CREATE_FOLDER_ENTRY, e);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * 
     * @param contentId
     * @param contentType
     * @return
     */
    @RequestMapping(value = "/api/folders/entries/{contentType}/{id}", method = DELETE)
    @ApiOperation(value = "Remove a FolderEntry")
    @Timed
    @VolumeMetered
    public void deleteFolderEntry(@PathVariable(value = "id") String contentId,
            @PathVariable(value = "contentType") String contentType, //
            @RequestParam String path) {
        try {
            final HystrixCommand<Void> createFolderEntry = getCommand(RemoveFolderEntry.class, getClient(), path, contentType,
                    contentId);
            createFolderEntry.execute();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_DELETE_FOLDER_ENTRY, e);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * 
     * @param path
     * @param contentType
     * @return
     */
    @RequestMapping(value = "/api/folders/entries", method = GET)
    @ApiOperation(value = "List all folder entries of the given content type within the path", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @VolumeMetered
    public void entries(@RequestParam String path, @RequestParam String contentType, final HttpServletResponse response) {
        try {
            final HystrixCommand<InputStream> listFolderEntries = getCommand(FolderEntriesList.class, getClient(), path,
                    contentType);
            response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$

            final ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(listFolderEntries.execute(), outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDER_ENTRIES, e);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     *
     * @param folder
     * @param sort
     * @param order
     * @return
     */
    @RequestMapping(value = "/api/folders/datasets", method = GET, consumes = ALL_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List all datasets within the folder and sorted by key/date", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @VolumeMetered
    public FolderContent datasets(
            @ApiParam(value = "Folder id to search datasets") @RequestParam(defaultValue = "", required = false) String folder,
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "DATE", required = false) String sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "DESC", required = false) String order,
            HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing datasets (pool: {})...", getConnectionManager().getTotalStats());
        }
        response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
        HttpClient client = getClient();
        HystrixCommand<FolderContent> listCommand = getCommand( FolderDataSetList.class, client, sort, order, folder);
        try {
            return listCommand.execute();
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
