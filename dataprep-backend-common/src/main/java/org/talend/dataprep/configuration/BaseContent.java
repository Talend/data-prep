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

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.info.VersionService;

/**
 * Provide instance for root/initial content with current application version.
 */
@Configuration
public class BaseContent {

    /** The version service. */
    @Autowired
    private VersionService versionService;

    /**
     * @return the preparation root content (no actions).
     */
    @Bean(name = "rootContent")
    public PreparationActions initRootContent() {
        return new PreparationActions(Collections.<Action> emptyList(), versionService.version().getVersionId());
    }

    /**
     * @return the default root step.
     */
    @Bean(name = "rootStep")
    public Step getRootStep(PreparationActions rootContent) {
        return new Step(null, rootContent.id(), versionService.version().getVersionId());
    }
}
