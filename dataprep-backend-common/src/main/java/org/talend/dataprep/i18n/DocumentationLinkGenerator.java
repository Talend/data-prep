// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.i18n;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder helping to generate documentation link
 */
public class DocumentationLinkGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentationLinkGenerator.class);

    private static final String LANGUAGE_PARAMETER = "afs:lang";

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String url;

        private Locale locale;

        public Builder url(final String url) {
            this.url = url;
            return this;
        }

        public Builder locale(final Locale locale) {
            this.locale = locale;
            return this;
        }

        public String build() {
            try {
                URI urlWithLangParameter = new URIBuilder(this.url) //
                        .addParameter(LANGUAGE_PARAMETER, String.valueOf(this.locale)) //
                        .build();
                return urlWithLangParameter.toString();
            } catch (URISyntaxException e) {
                LOGGER.error("{} is not a valid URL", this.url, e);
                return this.url;
            }
        }
    }
}
