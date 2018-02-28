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

package org.talend.dataprep.api.service.command.preparation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.service.command.AsyncGenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * Command used to retrieve the preparation content.
 */
@Component
@Scope("request")
public class PreparationGetMetadata extends AsyncGenericCommand<DataSetMetadata> {

    /** The preparation id. */
    private final String id;

    /** The preparation version. */
    private final String version;

    /**
     * @param id the preparation id.
     * @param version the preparation version.
     */
    private PreparationGetMetadata(String id, String version) {
        super(PREPARATION_GROUP);
        this.id = id;
        this.version = version;
        execute(() -> new HttpGet(transformationServiceUrl + "/apply/preparation/" + id + "/" + version + "/metadata"));
    }

    @PostConstruct
    public void init() {
        on(OK).then((req, resp) -> getResponseEntity(HttpStatus.OK, resp));
    }

    private ResponseEntity<DataSetMetadata> getResponseEntity(HttpStatus status, HttpResponse response) {

        final MultiValueMap<String, String> headers = new HttpHeaders();
        for (Header header : response.getAllHeaders()) {
            if("Location".equalsIgnoreCase(header.getName())) {
                headers.put(header.getName(), Collections.singletonList(header.getValue()));
            }
        }
        try {
            final InputStream content = response.getEntity().getContent();
            final String contentAsString = IOUtils.toString(content, UTF_8);
            DataSetMetadata result = objectMapper.readerFor(DataSetMetadata.class).readValue(contentAsString);
            return new ResponseEntity<>(result, headers,
                    status);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
