package org.talend.dataprep.api.service;

import com.netflix.hystrix.HystrixCommand;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.service.command.folder.CreateChildFolder;
import org.talend.dataprep.api.service.command.folder.FoldersList;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.metrics.Timed;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;


@RestController
@Api(value = "api", basePath = "/api", description = "Folders API")
public class FolderAPI extends APIService {


    @RequestMapping(value = "/api/folders/childs", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List childs folders of the parameter if null list root childs.")
    @Timed
    public void childs(@RequestParam(required = false)  String path, final HttpServletResponse response) {
        try {
            final HystrixCommand<InputStream> transformation = getCommand(FoldersList.class, getClient(), path);
            response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            final ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(transformation.execute(), outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_FOLDERS, e);
        }
    }


    @RequestMapping(value = "/api/folders/add", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add folder as a child of the one in parameter if none as child of root.")
    @Timed
    public void addFolder(@RequestParam(required = false) String parentPath, @RequestParam(required = true) String path, //
            final HttpServletResponse response) {
        try {
            final HystrixCommand<InputStream> transformation = getCommand(CreateChildFolder.class, getClient(), parentPath, path);
            response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            final ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(transformation.execute(), outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_CREATE_FOLDER, e);
        }
    }


}
