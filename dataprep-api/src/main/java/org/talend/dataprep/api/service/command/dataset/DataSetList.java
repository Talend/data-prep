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

package org.talend.dataprep.api.service.command.dataset;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;

import java.io.InputStream;
import java.net.URISyntaxException;

import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;

@Component
@Scope("request")
public class DataSetList extends GenericCommand<InputStream> {

    private DataSetList(Sort sort, Order order, String name, boolean certified, boolean favorite, boolean limit) {
        super(GenericCommand.DATASET_GROUP);

        try {
            execute(() -> onExecute(sort, order, name, certified, favorite, limit));
            onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_LIST_DATASETS, e));
            on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(emptyStream());
            on(HttpStatus.OK).then(pipeStream());

        } catch (Exception e) {
            throw new TDPException( CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    private HttpRequestBase onExecute(Sort sort, Order order,String name,  boolean certified, boolean favorite, boolean limit) {
        try {

            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/datasets");
            uriBuilder.addParameter( "sort", sort.camelName() );
            uriBuilder.addParameter( "order", order.camelName() );
            uriBuilder.addParameter( "name", name );
            uriBuilder.addParameter( "certified", Boolean.toString(certified));
            uriBuilder.addParameter( "favorite", Boolean.toString(favorite));
            uriBuilder.addParameter( "limit", Boolean.toString(limit));
            return new HttpGet( uriBuilder.build() );
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
