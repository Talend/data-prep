//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.settings.views.api.appheaderbar;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class LinkSettings {
    private String title;
    private String onClick;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOnClick() {
        return onClick;
    }

    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String onClick;

        public Builder title(final String title) {
            this.title = title;
            return this;
        }

        public Builder onClick(final String onClick) {
            this.onClick = onClick;
            return this;
        }

        public LinkSettings build() {
            final LinkSettings link = new LinkSettings();
            link.setTitle(this.title);
            link.setOnClick(this.onClick);
            return link;
        }
    }
}
