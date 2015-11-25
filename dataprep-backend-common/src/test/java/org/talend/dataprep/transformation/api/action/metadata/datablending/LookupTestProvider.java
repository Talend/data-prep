package org.talend.dataprep.transformation.api.action.metadata.datablending;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides Lookup over http connection.
 */
@RestController
public class LookupTestProvider {

    @RequestMapping(value = "/test/lookup/{id}", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getSampleRemoteFile(@PathVariable(value = "id") String lookupId) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(lookupId + ".json"));
    }

}
