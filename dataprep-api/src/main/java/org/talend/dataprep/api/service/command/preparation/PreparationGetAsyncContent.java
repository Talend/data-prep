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

package org.talend.dataprep.api.service.command.preparation;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.BaseErrorCodes.UNEXPECTED_EXCEPTION;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.annotation.PostConstruct;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.export.ExportParameters.SourceType;
import org.talend.dataprep.api.service.command.AsyncGenericCommand;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * Command used to retrieve the preparation content.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class PreparationGetAsyncContent extends AsyncGenericCommand<InputStream> {

    /**
     * @param id the preparation id.
     * @param version the preparation version.
     */
    private PreparationGetAsyncContent(String id, String version) {
        this(id, version, HEAD, null);
    }

    private PreparationGetAsyncContent(String id, String version, SourceType from) {
        this(id, version, from, null);
    }

    /**
     * @param id the preparation id.
     * @param version the preparation version.
     * @param from where to read the data from.
     */
    private PreparationGetAsyncContent(String id, String version, SourceType from, String filter) {
        super(PREPARATION_GROUP);

        execute(() -> {
            try {
                ExportParameters parameters = new ExportParameters();
                parameters.setPreparationId(id);
                parameters.setStepId(version);
                parameters.setExportType("JSON");
                parameters.setFrom(from);
                parameters.setFilter(filter);

                final String parametersAsString = objectMapper.writerFor(ExportParameters.class).writeValueAsString(parameters);
                final HttpPost post = new HttpPost(transformationServiceUrl + "/apply");
                post.setEntity(new StringEntity(parametersAsString, ContentType.APPLICATION_JSON));
                return post;
            } catch (Exception e) {
                throw new TDPException(UNEXPECTED_EXCEPTION, e);
            }
        });

        onError(Defaults.passthrough());
    }

    @PostConstruct
    public void init() {
        on(OK).then((req, resp) -> getResponseEntity(HttpStatus.OK, resp));
    }

    private ResponseEntity<InputStream> getResponseEntity(HttpStatus status, HttpResponse response) {

        final MultiValueMap<String, String> headers = new HttpHeaders();
        for (Header header : response.getAllHeaders()) {
            if ("Location".equalsIgnoreCase(header.getName())) {

                headers.put(header.getName(), Collections.singletonList(header.getValue()));
            }
        }
        try (final InputStream content = response.getEntity().getContent()) {
            return new ResponseEntity<>(content, headers, status);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
