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

package org.talend.dataprep.dataset.adapter.commands;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import javax.annotation.PostConstruct;
import java.util.stream.Stream;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component("DataSetList#2")
@Scope(SCOPE_PROTOTYPE)
public class DatasetList extends GenericCommand<Stream<Dataset>> {

    private DatasetList() {
        super(GenericCommand.DATASET_GROUP);
    }

    @PostConstruct
    private void initDataSetList() {
        try {
            execute(() -> new HttpGet(datasetServiceUrl + "/api/v1/datasets"));
            on(HttpStatus.OK).then(this::readResponse);

            onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_LIST_DATASETS, e));
            on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then((requestBase, response) -> Stream.empty());
        } catch (Exception e) {
            throw new TDPException( CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    private Stream<Dataset> readResponse(HttpRequestBase request, HttpResponse response) {
        Dataset[] dataSets = Defaults.convertResponse(objectMapper, Dataset[].class).apply(request, response);
        return Stream.of(dataSets);
    }

}
