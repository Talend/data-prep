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
import static org.talend.dataprep.BaseErrorCodes.UNEXPECTED_EXCEPTION;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;
import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.export.ExportParameters.SourceType;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

/**
 * Command used to retrieve the preparation content.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class PreparationGetContent extends GenericCommand<InputStream> {

    /**
     * @param id the preparation id.
     * @param version the preparation version.
     */
    private PreparationGetContent(String id, String version) {
        this(id, version, HEAD, null);
    }

    private PreparationGetContent(String id, String version, ExportParameters.SourceType from) {
        this(id, version, from, null);
    }

    /**
     * @param preparationId the preparation id.
     * @param version the preparation version.
     * @param from where to read the data from.
     */
    private PreparationGetContent(String preparationId, String version, SourceType from, String filter) {
        super(TRANSFORM_GROUP);

        execute(() -> {
            try {
                ExportParameters parameters = new ExportParameters();
                parameters.setPreparationId(preparationId);
                parameters.setStepId(version);
                parameters.setExportType("JSON");
                parameters.setFrom(from);
                parameters.setFilter(filter);

                final String parametersAsString =
                        objectMapper.writerFor(ExportParameters.class).writeValueAsString(parameters);
                final HttpPost post = new HttpPost(transformationServiceUrl + "/apply");
                post.setEntity(new StringEntity(parametersAsString, ContentType.APPLICATION_JSON));
                return post;
            } catch (Exception e) {
                final ExceptionContext context = ExceptionContext
                        .build()
                        .put("preparationId", preparationId)
                        .put("version", version)
                        .put("from", from)
                        .put("filter", filter);
                throw new TDPException(UNEXPECTED_EXCEPTION, e, context);
            }
        });
        on(HttpStatus.OK).then(pipeStream());
        on(HttpStatus.ACCEPTED).then(emptyStream());
        onError(Defaults.passthrough());
    }
}
