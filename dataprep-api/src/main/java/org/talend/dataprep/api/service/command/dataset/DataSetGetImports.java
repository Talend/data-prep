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

package org.talend.dataprep.api.service.command.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.Import;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.BiFunction;

import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

/**
 * Command to list dataset import types.
 */
@Component
@Scope("request")
public class DataSetGetImports extends GenericCommand<List<Import>> {

    public DataSetGetImports() {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> new HttpGet(datasetServiceUrl + "/datasets/imports"));
        on(HttpStatus.OK).then(toImportList());
    }

    /**
     * Convert HttpResponse to list of Import
     */
    private BiFunction<HttpRequestBase, HttpResponse, List<Import>> toImportList() {
        return (req, resp) -> {
            try (InputStream input = resp.getEntity().getContent()) {
                return objectMapper.readValue(input, new TypeReference<List<Import>>(){});
            } catch (IOException e) {
                throw new TDPException(UNEXPECTED_EXCEPTION, e);
            } finally {
                req.releaseConnection();
            }
        };
    }

}
