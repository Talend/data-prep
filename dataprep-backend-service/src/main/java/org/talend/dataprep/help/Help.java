package org.talend.dataprep.help;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Help {

    @Value("${help.documentation.search.url:https://www.talendforge.org/find/api/THC.php}")
    private String searchUrl;

    @Value("${help.documentation.fuzzy.url:}")
    private String fuzzyUrl;

    @Value("${help.documentation.exact.url:}")
    private String exactUrl;

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
