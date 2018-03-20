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

package org.talend.dataprep.api.service.command.transformation;

import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;


/**
 * Return the available semantic types for a preparation / column.
 */
@Component
@Scope("request")
public class GetPreparationColumnTypes extends GenericCommand<InputStream> {

    /**
     * Constructor.
     *
     * @param preparationId the preparation id.
     * @param columnId the column id.
     * @param stepId the step id.
     */
    private GetPreparationColumnTypes(String preparationId, String columnId, String stepId) {
        super(GenericCommand.TRANSFORM_GROUP);
        execute(onExecute(preparationId, columnId, stepId));
        on(HttpStatus.OK).then(pipeStream());
    }

    /**
     * @param preparationId the preparation id.
     * @param columnId the column id.
     * @param stepId the step id.
     * @return the http request to execute.
     */
    private Supplier<HttpRequestBase> onExecute(String preparationId, String columnId, String stepId) {
        return () -> {
            String uri = transformationServiceUrl + "/preparations/" + preparationId + "/columns/" + columnId + "/types";
            if (StringUtils.isNotBlank(stepId)) {
                uri += "?stepId="+stepId;
            }
            return new HttpGet(uri);
        };
    }

}
