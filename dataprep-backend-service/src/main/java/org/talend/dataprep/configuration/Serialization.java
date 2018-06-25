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

package org.talend.dataprep.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.api.preparation.json.MixedContentMapModule;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

@Configuration
@SuppressWarnings("InsufficientBranchCoverage")
public class Serialization {

    @Bean
    public Jdk8Module jdk8Module() {
        return new Jdk8Module();
    }

    @Bean
    public MixedContentMapModule mixedContentMapModule() {
        return new MixedContentMapModule();
    }

}
