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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.domain.Dataset;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;

@Component
@Scope(SCOPE_PROTOTYPE)
public class DataSetList extends GenericCommand<InputStream> {

    @Value("${dataset.api.usenew:false}")
    private Boolean useNewApi;

    private final Sort sort;

    private final Order order;

    private final String name;

    private final Boolean certified;

    private final Boolean favorite;

    private final Boolean limit;

    private final BeanConversionService beanConversionService;

    private DataSetList(Sort sort, Order order, String name, boolean certified, boolean favorite, boolean limit,
            BeanConversionService beanConversionService) {
        super(GenericCommand.DATASET_GROUP);
        this.sort = sort;
        this.order = order;
        this.name = name;
        this.certified = certified;
        this.favorite = favorite;
        this.limit = limit;
        this.beanConversionService = beanConversionService;
    }

    @PostConstruct
    private void initDataSetList() {
        try {
            if (useNewApi) {
                execute(() -> onExecute(sort, order, name, certified, favorite, limit));
                on(HttpStatus.OK).then(this::transformToLegacy);
            } else {
                execute(() -> onExecuteLegacy(sort, order, name, certified, favorite, limit));
                on(HttpStatus.OK).then(pipeStream());
            }
            onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_LIST_DATASETS, e));
            on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(emptyStream());

        } catch (Exception e) {
            throw new TDPException( CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    private HttpRequestBase onExecute(Sort sort, Order order,String name,  boolean certified, boolean favorite, boolean limit) {
        try {
            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/api/v1/datasets");
            return new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    private HttpRequestBase onExecuteLegacy(Sort sort, Order order,String name,  boolean certified, boolean favorite, boolean limit) {
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

    private InputStream transformToLegacy(HttpRequestBase request, HttpResponse response) {
        Dataset[] dataSets = Defaults.convertResponse(objectMapper, Dataset[].class).apply(request, response);

        List<DataSetMetadata> metadata = Arrays.stream(dataSets).map(this::toDataSetMetadata).collect(Collectors.toList());

        // Totally not stream => POC !
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);) {
            objectMapper.writerFor(DataSetMetadata.class).writeValues(writer).writeAll(metadata);
            writer.flush();
            outputStream.flush();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    private DataSetMetadata toDataSetMetadata(Dataset dataset) {
        return beanConversionService.convert(dataset, DataSetMetadata.class);
    }

}
