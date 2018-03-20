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

import static org.talend.dataprep.command.Defaults.convertResponse;

import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.command.GenericCommand;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Hystrix command used to update a step row metadata.
 */
@Component
@Scope("prototype")
public class GetStepRowMetadata extends GenericCommand<RowMetadata> {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Private constructor to ensure the IoC.
     */
    private GetStepRowMetadata(String stepId) {
        super(PREPARATION_GROUP);
        execute(() -> new HttpGet(preparationServiceUrl + "/preparations/steps/" + stepId + "/metadata"));
    }

    @PostConstruct
    public void init() {
        on(HttpStatus.OK).then(convertResponse(objectMapper, RowMetadata.class));
    }

}
