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

package org.talend.dataprep.api.service.command.export;

import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

/**
 * Command used to start an export of a dataset / preparation.
 */
@Component
@Scope("request")
public class Export extends GenericCommand<InputStream> {

    /**
     * @param parameters the export parameters.
     */
    // private constructor to ensure the IoC
    private Export(final ExportParameters parameters) {
        super(TRANSFORM_GROUP);
        execute(() -> onExecute(parameters));
        on(HttpStatus.OK).then(pipeStream());
    }

    /**
     * @param parameters the export parameters.
     * @return the request to perform.
     */
    private HttpRequestBase onExecute(ExportParameters parameters) {
        try {
            final String parametersAsString = objectMapper.writerFor(ExportParameters.class).writeValueAsString(parameters);
            final HttpPost post = new HttpPost(transformationServiceUrl + "/apply");
            post.setEntity(new StringEntity(parametersAsString, ContentType.APPLICATION_JSON));
            return post;
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_EXPORT_CONTENT, e);
        }
    }

}
