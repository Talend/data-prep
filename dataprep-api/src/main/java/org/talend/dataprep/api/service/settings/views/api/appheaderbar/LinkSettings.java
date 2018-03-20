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

package org.talend.dataprep.api.service.settings.views.api.appheaderbar;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.i18n.DataprepBundle;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Simple link configuration
 */
@JsonInclude(NON_NULL)
public class LinkSettings {

    /**
     * The display / tooltip title
     */
    private String title;

    /**
     * The display name
     */
    private String name;

    /**
     * The display label
     */
    private String label;

    /**
     * The action identifier
     */
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static LinkSettings.Builder from(final LinkSettings linkSettings) {
        return builder() //
                .title(linkSettings.getTitle()) //
                .name(linkSettings.getName()) //
                .label(linkSettings.getLabel()) //
                .onClick(linkSettings.getOnClick());
    }

    public static class Builder {

        private String title;

        private String name;

        private String label;

        private String onClick;

        public Builder title(final String title) {
            this.title = title;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder label(final String label) {
            this.label = label;
            return this;
        }

        public Builder onClick(final String onClick) {
            this.onClick = onClick;
            return this;
        }

        public Builder translate() {
            if (StringUtils.isNotEmpty(this.title)) {
                this.title = DataprepBundle.message(this.title);
            }
            if (StringUtils.isNotEmpty(this.name)) {
                this.name = DataprepBundle.message(this.name);
            }
            if (StringUtils.isNotEmpty(this.label)) {
                this.label = DataprepBundle.message(this.label);
            }
            return this;
        }

        public LinkSettings build() {
            final LinkSettings link = new LinkSettings();
            link.setTitle(this.title);
            link.setName(this.name);
            link.setOnClick(this.onClick);
            link.setLabel(this.label);
            return link;
        }
    }
}
