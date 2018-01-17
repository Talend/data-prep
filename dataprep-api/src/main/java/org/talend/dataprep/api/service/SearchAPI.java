// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import java.io.OutputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.service.delegate.SearchDelegate;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.util.OrderedBeans;

import com.fasterxml.jackson.core.JsonGenerator;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API in charge of the search.
 */
@RestController
public class SearchAPI extends APIService {

    @Autowired
    private MessagesBundle messagesBundle;

    @Autowired
    @Qualifier("ordered#search")
    private OrderedBeans<SearchDelegate> searchDelegates;

    /**
     * Search dataprep folders, preparations and datasets.
     *
     * @param name the name searched.
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
        return output -> doSearch(name, filter, strict, output);
    }

    private void doSearch(String name, List<String> filter, boolean strict, OutputStream output) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching dataprep for '{}' (pool: {})...", name, getConnectionStats());
        }
        try (final JsonGenerator generator = mapper.getFactory().createGenerator(output)) {
            generator.writeStartObject();

            // Write category information
            generator.writeFieldName("categories");
            generator.writeStartArray();

            // Add static information about documentation category
            generator.writeStartObject();
            generator.writeStringField("type", "documentation");
            generator.writeStringField("label",
                    messagesBundle.getString(LocaleContextHolder.getLocale(), "search.documentation"));
            generator.writeEndObject();

            // Now the search types categories
            searchDelegates.forEach(searchDelegate -> {
                final String categoryLabel =
                        messagesBundle.getString(LocaleContextHolder.getLocale(), "search." + searchDelegate.getSearchLabel());
                try {
                    generator.writeStartObject();
                    generator.writeStringField("type", searchDelegate.getInventoryType());
                    generator.writeStringField("label", categoryLabel);
                    generator.writeEndObject();
                } catch (IOException e) {
                    LOG.error("Unable to write category information for '{}'.", searchDelegate.getSearchCategory(), e);
                }
            });
            generator.writeEndArray();

            // Write results
            searchDelegates.forEach(searchDelegate -> {
                final String category = searchDelegate.getSearchCategory();
                if (filter == null || filter.contains(category)) {
                    try {
                        generator.writeObjectField(category, searchDelegate.search(name, strict));
                    } catch (IOException e) {
                        LOG.error("Unable to search '{}'.", category, e);
                    }
                }
            });
            generator.writeEndObject();
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_SEARCH_DATAPREP, e);
        }
        LOG.debug("Search done on for '{}' with filter '{}' (strict mode: {})", name, filter, strict);
    }
}
