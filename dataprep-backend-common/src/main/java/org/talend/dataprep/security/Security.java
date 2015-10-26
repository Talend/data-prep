package org.talend.dataprep.security;

public interface Security {

    /**
     * @return Get user id based on the user name from Spring Security context, return "anonymous" if no user is
     * currently logged in.
     */
    String getUserId();
}
