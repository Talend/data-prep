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

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class SearchSettings {
    private int debounceTimeout;
    private String placeholder;
    private String onBlur;
    private String onChange;
    private String onKeyDown;
    private String onToggle;
    private Map<String, String> onSelect;

    public int getDebounceTimeout() {
        return debounceTimeout;
    }

    public void setDebounceTimeout(int debounceTimeout) {
        this.debounceTimeout = debounceTimeout;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getOnBlur() {
        return onBlur;
    }

    public void setOnBlur(String onBlur) {
        this.onBlur = onBlur;
    }

    public String getOnChange() {
        return onChange;
    }

    public void setOnChange(String onChange) {
        this.onChange = onChange;
    }

    public String getOnKeyDown() {
        return onKeyDown;
    }

    public void setOnKeyDown(String onKeyDown) {
        this.onKeyDown = onKeyDown;
    }

    public String getOnToggle() {
        return onToggle;
    }

    public void setOnToggle(String onToggle) {
        this.onToggle = onToggle;
    }

    public Map<String, String> getOnSelect() {
        return onSelect;
    }

    public void setOnSelect(Map<String, String> onSelect) {
        this.onSelect = onSelect;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int debounceTimeout;
        private String placeholder;
        private String onBlur;
        private String onChange;
        private String onKeyDown;
        private String onToggle;
        private Map<String, String> onSelect = new HashMap<>();

        public Builder debounceTimeout(final int debounceTimeout) {
            this.debounceTimeout = debounceTimeout;
            return this;
        }

        public Builder placeholder(final String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder onBlur(final String onBlur) {
            this.onBlur = onBlur;
            return this;
        }

        public Builder onChange(final String onChange) {
            this.onChange = onChange;
            return this;
        }

        public Builder onKeyDown(final String onKeyDown) {
            this.onKeyDown = onKeyDown;
            return this;
        }

        public Builder onToggle(final String onToggle) {
            this.onToggle = onToggle;
            return this;
        }

        public Builder onSelect(final String type, final String onSelect) {
            this.onSelect.put(type, onSelect);
            return this;
        }

        public SearchSettings build() {
            final SearchSettings searchSettings = new SearchSettings();
            searchSettings.setDebounceTimeout(this.debounceTimeout);
            searchSettings.setPlaceholder(this.placeholder);
            searchSettings.setOnBlur(this.onBlur);
            searchSettings.setOnChange(this.onChange);
            searchSettings.setOnKeyDown(this.onKeyDown);
            searchSettings.setOnToggle(this.onToggle);
            searchSettings.setOnSelect(this.onSelect);
            return searchSettings;
        }
    }
}
