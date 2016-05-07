// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.api.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.service.command.datepattern.AllDatePatternsList;
import org.talend.dataprep.api.service.command.datepattern.CreateDatepattern;
import org.talend.dataprep.datepattern.DatePattern;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.metrics.Timed;

import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;

import javax.ws.rs.QueryParam;

@RestController
public class DatePatternAPI extends APIService {

    @RequestMapping(value = "/api/datepatterns", method = GET)
    @ApiOperation(value = "List all datepattern.", produces = APPLICATION_JSON_VALUE)
    @Timed
    public void allDatepattern( OutputStream output, @RequestParam(required = false) String keyword) {
        HystrixCommand<InputStream> foldersList = getCommand( AllDatePatternsList.class, keyword);
        try (InputStream commandResult = foldersList.execute()){
            HttpResponseContext.header( "Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            IOUtils.copyLarge( commandResult, output);
            output.flush();
        } catch (Exception e) {
            throw new TDPException( APIErrorCodes.UNABLE_TO_LIST_DATEPATTERNS, e);
        }
    }

    @RequestMapping(value = "/api/datepatterns", method = PUT)
    @ApiOperation(value = "create datepattern.", consumes = APPLICATION_JSON_VALUE)
    @Timed
    public void createDatepattern(@RequestParam String pattern) {

        HystrixCommand<Void> createCommand = getCommand(CreateDatepattern.class, new DatePattern(pattern));
        try {
            createCommand.execute();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_CREATE_DATEPATTERN, e);
        }
    }

}
