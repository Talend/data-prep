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

package org.talend.dataprep.configuration;

import static org.talend.dataprep.api.preparation.PreparationActions.ROOT_ACTIONS;
import static org.talend.dataprep.api.preparation.Step.ROOT_STEP;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provide instance for root/initial content with current application version.
 */
@Configuration
public class BaseContent {

    /** The version service. */
    @Autowired
    private VersionService versionService;

    @Bean
    public Converter<String, JsonNode> jsonNodeConverter() {
        return source -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readTree(source);
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        };
    }

    /**
     * @return the preparation root content (no actions).
     */
    @Bean(name = "rootContent")
    public PreparationActions initRootContent() {
        PreparationActions preparationActions = new PreparationActions( //
                Collections.emptyList(), //
                versionService.version().getVersionId() //
        );
        preparationActions.setId(ROOT_ACTIONS.id());
        return preparationActions;
    }

    /**
     * @return the default root step.
     */
    @Bean(name = "rootStep")
    public Step getRootStep(PreparationActions rootContent) {
        final Step step = new Step(null, rootContent, versionService.version().getVersionId());
        step.setId(ROOT_STEP.id());
        return step;
    }
}
