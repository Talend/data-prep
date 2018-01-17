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

package org.talend.dataprep.api.service.command.preparation;

import static org.talend.dataprep.command.Defaults.asNull;

import org.apache.http.client.methods.HttpDelete;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;

@Component
@Scope("request")
public class PreparationDeleteAction extends GenericCommand<Void> {

    private PreparationDeleteAction(final String preparationId, final String stepId) {
        super(GenericCommand.PREPARATION_GROUP);
        execute(() -> new HttpDelete(preparationServiceUrl + "/preparations/" + preparationId + "/actions/" + stepId)); //$NON-NLS-1$ //$NON-NLS-2$
        on(HttpStatus.OK).then(asNull());
    }

}
