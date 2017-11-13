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
        return this.exactUrl;
    }

}
