package org.talend.dataprep.security;

import org.springframework.stereotype.Component;

@Component
public class NoOpSecurity implements Security {

    @Override
    public String getUserId() {
        return "anonymousUser";
    }
}
