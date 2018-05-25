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

package org.talend.dataprep.transformation.actions.net;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIUtils;
import org.talend.dataprep.api.type.Type;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

/**
 * Class that holds all the url extractors.
 */
class UrlTokenExtractors {

    /**
     * Extracts the protocol.
     */
    private static final UrlTokenExtractor PROTOCOL_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_protocol";
        }

        @Override
        public String extractToken(URI uri) {
            final String scheme = uri.getScheme();
            return scheme == null ? null : scheme.toLowerCase();
        }
    };

    /**
     * Extracts the host.
     */
    private static final UrlTokenExtractor HOST_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_host";
        }

        @Override
        public String extractToken(URI uri) {
            // URI.getHost() is bugged, @see https://bugs.java.com/view_bug.do?bug_id=6587184
            HttpHost httpHost = URIUtils.extractHost(uri);
            return httpHost == null ? null : httpHost.getHostName();
        }
    };

    /**
     * Extracts the port, if any.
     */
    private static final UrlTokenExtractor PORT_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_port";
        }

        @Override
        public String extractToken(URI uri) {
            int port = uri.getPort();
            if (port == -1) {
                String authority = uri.getAuthority();
                if (authority != null) {
                    String endAfterColon = StringUtils.substringAfterLast(authority, ":");
                    if (StringUtils.isNumeric(endAfterColon)) {
                        port = Integer.parseInt(endAfterColon);
                    }
                }
            }
            return port == -1 ? null : Integer.toString(port);
        }

        @Override
        public Type getType() {
            return Type.INTEGER;
        }

    };

    /**
     * Extracts the path.
     */
    private static final UrlTokenExtractor PATH_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_path";
        }

        @Override
        public String extractToken(URI uri) {
            return uri.getPath();
        }
    };

    /**
     * Extracts the query, if any.
     */
    private static final UrlTokenExtractor QUERY_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_query";
        }

        @Override
        public String extractToken(URI uri) {
            return uri.getQuery();
        }
    };

    /**
     * Extracts the fragment.
     */
    private static final UrlTokenExtractor FRAGMENT_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_fragment";
        }

        @Override
        public String extractToken(URI uri) {
            return uri.getFragment();
        }
    };

    /**
     * Extracts the user, if any.
     */
    private static final UrlTokenExtractor USER_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_user";
        }

        @Override
        public String extractToken(URI uri) {
            return extractUserInfo(uri)[0];
        }
    };

    /**
     * Extracts the password, if any.
     */
    private static final UrlTokenExtractor PASSWORD_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_password";
        }

        @Override
        public String extractToken(URI uri) {
            return extractUserInfo(uri)[1];
        }
    };

    private static String[] extractUserInfo(URI uri) {
        String userInfo = uri.getUserInfo();
        if (userInfo == null) {
            // try using URL parsing
            try {
                // circumvent the URL in-built scheme validation that wouldn't validate mvn protocol for instance
                userInfo = new URL("http://" + uri.getAuthority()).getUserInfo();
            } catch (MalformedURLException e) {
                // ignoring
            }
            if (userInfo == null) {
                // try manual extraction
                String authority = uri.getAuthority();
                if (authority != null && authority.contains("@")) {
                    userInfo = StringUtils.substringBefore(authority, "@");
                }
            }
        }
        return userInfo == null ? new String[2] : Arrays.copyOf(StringUtils.split(userInfo, ':'), 2);
    }

    /**
     * List all the available extractors.
     */
    static UrlTokenExtractor[] URL_TOKEN_EXTRACTORS = new UrlTokenExtractor[] { PROTOCOL_TOKEN_EXTRACTOR,
            HOST_TOKEN_EXTRACTOR, PORT_TOKEN_EXTRACTOR, PATH_TOKEN_EXTRACTOR, QUERY_TOKEN_EXTRACTOR, FRAGMENT_TOKEN_EXTRACTOR,
            USER_TOKEN_EXTRACTOR, PASSWORD_TOKEN_EXTRACTOR };

}
