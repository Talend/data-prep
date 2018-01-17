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

import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;
import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.http.client.methods.HttpPost;
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
 * Command used to retrieve the preparation content.
 */
@Component
@Scope("request")
public class PreparationGetContent extends GenericCommand<InputStream> {

    /** The preparation id. */
    private final String id;

    /** The preparation version. */
    private final String version;

    /**
     * @param id the preparation id.
     * @param version the preparation version.
     */
    private PreparationGetContent(String id, String version) {
        this(id, version, HEAD);
    }

    /**
     * @param id the preparation id.
     * @param version the preparation version.
     * @param from where to read the data from.
     */
    private PreparationGetContent(String id, String version, ExportParameters.SourceType from) {
        super(PREPARATION_GROUP);
        this.id = id;
        this.version = version;
        execute(() -> {
            try {
                ExportParameters parameters = new ExportParameters();
                parameters.setPreparationId(this.id);
                parameters.setStepId(this.version);
                parameters.setExportType("JSON");
                parameters.setFrom(from);

                final String parametersAsString = objectMapper.writerFor(ExportParameters.class).writeValueAsString(parameters);
                final HttpPost post = new HttpPost(transformationServiceUrl + "/apply");
                post.setEntity(new StringEntity(parametersAsString, ContentType.APPLICATION_JSON));
                return post;
            } catch (Exception e) {
                throw new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_PREPARATION_CONTENT, e);
            }
        });
        on(HttpStatus.OK).then(pipeStream());
    }

}
