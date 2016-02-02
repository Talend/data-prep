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

package org.talend.dataprep.i18n;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
@SuppressWarnings("InsufficientBranchCoverage")
public class I18N {

    @Bean
    public ResourceBundleMessageSource getResourceBundle() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        String[] baseNames = new String[] { "org.talend.dataprep.messages", "org.talend.dataprep.error_messages" };
        source.setBasenames(baseNames); // $NON-NLS-1$
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }
}
