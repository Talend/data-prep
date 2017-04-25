// ============================================================================
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
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_SEARCH_DATAPREP;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.service.delegate.SearchDelegate;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.metrics.Timed;

import com.fasterxml.jackson.core.JsonGenerator;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API in charge of the search.
 */
@RestController
public class SearchAPI extends APIService {

    @Autowired
    private List<SearchDelegate> searchDelegates;

    /**
     * Search dataprep folders, preparations and datasets.
     *
     * @param query the name searched.
     * @param filter the types of items to search. It can be (dataset, preparation, folder).
     * @param strict strict mode means that the name should be the full name (still case insensitive).
     */
    //@formatter:off
    @RequestMapping(value = "/api/search", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "List the of elements contained in a folder matching the given name", produces = APPLICATION_JSON_VALUE)
    @Timed
    public StreamingResponseBody search(
            @ApiParam(value = "name") @RequestParam(defaultValue = "", required = false) final String name,
            @ApiParam(value = "filter") @RequestParam(required = false) final List<String> filter,
            @ApiParam(value = "strict") @RequestParam(defaultValue = "false", required = false) final boolean strict) {
    //@formatter:on
        return output -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Searching dataprep for '{}' (pool: {})...", name, getConnectionStats());
            }
            try (final JsonGenerator generator = mapper.getFactory().createGenerator(output)) {
                generator.writeStartObject();
                for (SearchDelegate searchDelegate : searchDelegates) {
                    if (filter == null || filter.contains(searchDelegate.getSearchCategory())) {
                        try {
                            generator.writeFieldName(searchDelegate.getSearchCategory());
                            generator.writeObject(searchDelegate.search(name, strict));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                generator.writeEndObject();

            } catch (IOException e) {
                throw new TDPException(UNABLE_TO_SEARCH_DATAPREP, e);
            }
            LOG.info("Searching Done on dataprep for {} done with filter: {} and strict mode: {}", name, filter, strict);
        };
    }
}
