// ============================================================================
//
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

package org.talend.dataprep.command.dataset;

import static org.talend.dataprep.command.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

/**
 * Command used to search datasets.
 */
@Component
@Scope("prototype")
public class SearchDataSets extends GenericCommand<InputStream> {

    /**
     * Default constructor.
     *
     * @param name the name to search.
     */
    // private constructor to ensure IoC
    private SearchDataSets(final String name, final boolean strict) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> onExecute(name, strict));
        on(HttpStatus.OK).then(pipeStream());

    }

    private HttpRequestBase onExecute(final String name, final boolean strict) {
        try {
            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/datasets/search");
            uriBuilder.addParameter("name", name);
            uriBuilder.addParameter("strict", String.valueOf(strict));
            return new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
    }
}
