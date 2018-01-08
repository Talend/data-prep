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

package org.talend.dataprep.i18n;

import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder helping to generate documentation link
 */
public class DocumentationLinkGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentationLinkGenerator.class);

    private static final String AFS_LANGUAGE_PARAMETER = "afs:lang";

    private static final String CONTENT_LANG_PARAMETER = "content-lang";

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String url;

        private Locale locale;

        private boolean addAfsLanguageParameter;

        private boolean addContentLangParameter;

        public Builder url(final String url) {
            this.url = url;
            return this;
        }

        public Builder locale(final Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder addAfsLanguageParameter(final boolean addAfsLanguageParameter) {
            this.addAfsLanguageParameter = addAfsLanguageParameter;
            return this;
        }

        public Builder addContentLangParameter(final boolean addContentLangParameter) {
            this.addContentLangParameter = addContentLangParameter;
            return this;
        }

        public String build() {
            if (StringUtils.isEmpty(this.url)) {
                return this.url;
            }
            try {
                URIBuilder urlWithLangParameter = new URIBuilder(this.url);
                if (this.addAfsLanguageParameter && Objects.nonNull(this.locale)) {
                    urlWithLangParameter.addParameter(AFS_LANGUAGE_PARAMETER, this.locale.getLanguage());
                }

                if (this.addContentLangParameter && Objects.nonNull(this.locale)) {
                    urlWithLangParameter.addParameter(CONTENT_LANG_PARAMETER, this.locale.getLanguage());
                }

                return urlWithLangParameter.build().toString();
            } catch (URISyntaxException e) {
                LOGGER.error("{} is not a valid URL", this.url, e);
                return this.url;
            }
        }
    }
}
