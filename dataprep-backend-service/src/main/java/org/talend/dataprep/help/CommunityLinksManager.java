package org.talend.dataprep.help;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommunityLinksManager {

    @Value("${community.url:}")
    private String communityUrl;

    public String getCommunityUrl() {
        return communityUrl;
    }
}
