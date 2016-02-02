//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.dataset.store.content.http;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mock remote http server used to check the remote http dataset import.
 */
@RestController
public class MockRemoteHttpServer {

    @RequestMapping(value = "/not/so/far/away", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String getSampleRemoteFile() throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream("file.csv"));
    }

    @RequestMapping(value = "/cannot/be/reached", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String wontGetSampleRemoteFile() {
        throw new ResourceNotFoundException();
    }

}
