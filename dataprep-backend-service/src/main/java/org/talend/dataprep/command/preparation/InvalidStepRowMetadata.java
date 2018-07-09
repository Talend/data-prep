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

package org.talend.dataprep.command.preparation;

import static org.talend.dataprep.command.Defaults.asNull;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;

/**
 * Hystrix command used to invalidate a step row metadata.
 */
@Component
@Scope("prototype")
public class InvalidStepRowMetadata extends GenericCommand<Void> {

    /**
     * Private constructor to ensure the IoC.
     *
     * @param stepId the step id to update .
     */
    private InvalidStepRowMetadata(String stepId) {
        super(PREPARATION_GROUP);
        execute(() -> onExecute(stepId));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute(String stepId) {
        return new HttpDelete(preparationServiceUrl + "/preparations/steps/" + stepId + "/metadata");
    }
}
