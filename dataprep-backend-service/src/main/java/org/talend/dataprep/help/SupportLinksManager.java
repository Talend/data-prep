package org.talend.dataprep.help;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SupportLinksManager {

    @Value("${support.url:}")
    private String supportUrl;

    public String getSupportUrl() {
        return supportUrl;
    }
}
