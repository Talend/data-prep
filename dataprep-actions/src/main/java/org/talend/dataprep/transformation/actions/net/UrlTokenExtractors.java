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

import java.net.URI;

import org.talend.dataprep.api.type.Type;

/**
 * Class that holds all the url extractors.
 */
public class UrlTokenExtractors {

    /**
     * Extracts the protocol.
     */
    protected static final UrlTokenExtractor PROTOCOL_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_protocol";
        }

        @Override
        public String extractToken(URI url) {
            final String scheme = url.getScheme();
            return scheme == null ? "" : scheme.toLowerCase();
        }
    };

    /**
     * Extracts the host.
     */
    protected static final UrlTokenExtractor HOST_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_host";
        }

        @Override
        public String extractToken(URI url) {
            return url.getHost();
        }
    };

    /**
     * Extracts the port, if any.
     */
    protected static final UrlTokenExtractor PORT_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_port";
        }

        @Override
        public String extractToken(URI url) {
            final int port = url.getPort();
            return port == -1 ? "" : port + "";
        }

        @Override
        public Type getType() {
            return Type.INTEGER;
        }

    };

    /**
     * Extracts the path.
     */
    protected static final UrlTokenExtractor PATH_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_path";
        }

        @Override
        public String extractToken(URI url) {
            return url.getPath();
        }
    };

    /**
     * Extracts the query, if any.
     */
    protected static final UrlTokenExtractor QUERY_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_query";
        }

        @Override
        public String extractToken(URI url) {
            return url.getQuery();
        }
    };

    /**
     * Extracts the fragment.
     */
    protected static final UrlTokenExtractor FRAGMENT_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_fragment";
        }

        @Override
        public String extractToken(URI url) {
            return url.getFragment();
        }
    };

    /**
     * Extracts the user, if any.
     */
    protected static final UrlTokenExtractor USER_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_user";
        }

        @Override
        public String extractToken(URI url) {
            final String userInfo = url.getUserInfo();
            return userInfo == null ? "" : userInfo.split(":")[0];
        }
    };

    /**
     * Extracts the password, if any.
     */
    protected static final UrlTokenExtractor PASSWORD_TOKEN_EXTRACTOR = new UrlTokenExtractor() {

        @Override
        public String getTokenName() {
            return "_password";
        }

        @Override
        public String extractToken(URI url) {
            final String userInfo = url.getUserInfo();
            return userInfo == null ? "" : userInfo.split(":")[1];
        }
    };

    /**
     * List all the available extractors.
     */
    protected static UrlTokenExtractor[] urlTokenExtractors = new UrlTokenExtractor[] { PROTOCOL_TOKEN_EXTRACTOR,
            HOST_TOKEN_EXTRACTOR, PORT_TOKEN_EXTRACTOR, PATH_TOKEN_EXTRACTOR, QUERY_TOKEN_EXTRACTOR, FRAGMENT_TOKEN_EXTRACTOR,
            USER_TOKEN_EXTRACTOR, PASSWORD_TOKEN_EXTRACTOR };

}
