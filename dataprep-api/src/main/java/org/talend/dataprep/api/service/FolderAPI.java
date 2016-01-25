package org.talend.dataprep.api.service;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.api.folder.FolderContent;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.service.command.common.HttpResponse;
import org.talend.dataprep.api.service.command.dataset.CloneDataSet;
import org.talend.dataprep.api.service.command.folder.*;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;

import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
public class FolderAPI extends APIService {

    @RequestMapping(value = "/api/folders", method = GET)
    @ApiOperation(value = "List children folders of the parameter if null list root children.", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void children(@RequestParam(required = false) String path, final OutputStream output) {
        try {
            final HystrixCommand<InputStream> foldersList = getCommand(FoldersList.class, getClient(), path);
            HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            IOUtils.copyLarge(foldersList.execute(), output);
            output.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDERS, e);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * 
     * @param pathName
     * @return
     */
    @RequestMapping(value = "/api/folders/search", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Search Folders with parameter as part of the name", produces = MediaType.APPLICATION_JSON_VALUE, notes = "")
    @Timed
    public void search(@RequestParam(required = false) String pathName, final OutputStream output) {
        try {
            final HystrixCommand<InputStream> searchFolders = getCommand(SearchFolders.class, getClient(), pathName);
            HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            IOUtils.copyLarge(searchFolders.execute(), output);
            output.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDERS, e);
        }
    }

    @RequestMapping(value = "/api/folders/all", method = GET)
    @ApiOperation(value = "List all folders.", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void allFolder(final OutputStream output) {
        try {
            final HystrixCommand<InputStream> foldersList = getCommand(AllFoldersList.class, getClient());
            HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            IOUtils.copyLarge(foldersList.execute(), output);
            output.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDERS, e);
        }
    }

    @RequestMapping(value = "/api/folders", method = PUT)
    @ApiOperation(value = "Add a folder.", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void addFolder(@RequestParam(required = true) String path, //
            final OutputStream output) {
        try {
            final HystrixCommand<InputStream> createChildFolder = getCommand(CreateChildFolder.class, getClient(), path);
            HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            IOUtils.copyLarge(createChildFolder.execute(), output);
            output.flush();
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
    public void removeFolder(@RequestParam(required = true) String path, final OutputStream output) {
        try {
            final HystrixCommand<HttpResponse> removeFolder = getCommand(RemoveFolder.class, getClient(), path);
            HttpResponse result = removeFolder.execute();
            try {
                HttpResponseContext.status( HttpStatus.valueOf( result.getStatusCode()));
                HttpResponseContext.header("Content-Type", result.getContentType());
                IOUtils.write(result.getHttpContent(), output);
                output.flush();
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }

        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_DELETE_FOLDER, e);
        }
    }

    /**
     * no javadoc here so see description in @ApiOperation notes.
     * 
     * @param path
     * @param newPath
     */
    @RequestMapping(value = "/api/folders/rename", method = PUT)
    @ApiOperation(value = "Rename a Folder")
    @Timed
    public void renameFolder(@RequestParam String path, @RequestParam String newPath) {

        if (StringUtils.isEmpty(path) //
                || StringUtils.isEmpty(newPath) //
                || StringUtils.containsOnly(path, "/")) {

            throw new TDPException(APIErrorCodes.UNABLE_TO_RENAME_FOLDER);
        }
        try {
            final HystrixCommand<Void> renameFolder = getCommand(RenameFolder.class, getClient(), path, newPath);
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
    public void addFolderEntry(@RequestBody FolderEntry folderEntry, final OutputStream output) {
        try {
            final HystrixCommand<InputStream> createFolderEntry = getCommand(CreateFolderEntry.class, getClient(), folderEntry);
            HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            IOUtils.copyLarge(createFolderEntry.execute(), output);
            output.flush();
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
    @ApiOperation(value = "Remove a Folder Entry")
    @Timed
    public void deleteFolderEntry(@PathVariable(value = "id") String contentId,
            @PathVariable(value = "contentType") String contentType, //
            @RequestParam String path) {
        try {
            final HystrixCommand<Void> removeFolderEntry = getCommand(RemoveFolderEntry.class, getClient(), path, contentType,
                    contentId);
            removeFolderEntry.execute();
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
    public void entries(@RequestParam String path, @RequestParam String contentType, final OutputStream output) {
        try {
            final HystrixCommand<InputStream> listFolderEntries = getCommand(FolderEntriesList.class, getClient(), path,
                    contentType);
            HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$

            IOUtils.copyLarge(listFolderEntries.execute(), output);
            output.flush();
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
    public FolderContent datasets(
            @ApiParam(value = "Folder id to search datasets") @RequestParam(defaultValue = "", required = false) String folder,
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "DATE", required = false) String sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "DESC", required = false) String order) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing datasets (pool: {})...", getConnectionStats());
        }
        HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
        HttpClient client = getClient();
        HystrixCommand<FolderContent> listCommand = getCommand(FolderDataSetList.class, client, sort, order, folder);
        try {
            return listCommand.execute();
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
