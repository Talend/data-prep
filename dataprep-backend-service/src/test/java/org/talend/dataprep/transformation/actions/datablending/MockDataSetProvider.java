//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.actions.datablending;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides Lookup over http connection.
 */
@RestController
@Profile("backend-common")
public class MockDataSetProvider {

    /**
     * This request mapping must follow the dataset request mapping
     *
     * @param lookupId
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/datasets/{id}/content", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getSampleRemoteFile(@PathVariable(value = "id") String lookupId) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(lookupId + ".json"), UTF_8);
    }

}
