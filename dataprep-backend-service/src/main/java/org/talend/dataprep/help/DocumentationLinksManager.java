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

package org.talend.dataprep.help;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DocumentationLinksManager {

    @Value("${help.facets.version:}")
    private String versionFacet;

    @Value("${help.search.url:https://www.talendforge.org/find/api/THC.php}")
    private String searchUrl;

    @Value("${help.fuzzy.url:}")
    private String fuzzyUrl;

    @Value("${help.exact.url:}")
    private String exactUrl;

    public String getVersionFacet() {
        return versionFacet;
    }

    public String getLanguageFacet() {
        return LocaleContextHolder.getLocale().getLanguage();
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public String getFuzzyUrl() {
        return fuzzyUrl;
    }

    public String getExactUrl() {
        return exactUrl;
    }

}
