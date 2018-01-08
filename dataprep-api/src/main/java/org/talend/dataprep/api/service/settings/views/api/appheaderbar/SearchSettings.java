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

package org.talend.dataprep.api.service.settings.views.api.appheaderbar;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.LinkedHashMap;
import java.util.Map;

import org.talend.dataprep.i18n.DataprepBundle;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Search bar settings
 * see
 * https://talend.github.io/react-talend-components/?selectedKind=App%20Header%20Bar&selectedStory=with%20search%20results&full=0&down=1&left=1&panelRight=0&downPanel=kadirahq%2Fstorybook-addon-actions%2Factions-panel
 */
@JsonInclude(NON_NULL)
public class SearchSettings {

    /**
     * Time in ms of idle time after user typing, before triggering the action
     */
    private int debounceTimeout;

    /**
     * Input placeholder
     */
    private String placeholder;

    /**
     * Input blur action identifier
     */
    private String onBlur;

    /**
     * Input change action identifier
     */
    private String onChange;

    /**
     * Input keydown action identifier
     */
    private String onKeyDown;

    /**
     * Toggle icon action identifier
     */
    private String onToggle;

    /**
     * Result item selection action by type of item
     */
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

    public static Builder from(final SearchSettings viewSettings) {
        return builder() //
                .debounceTimeout(viewSettings.getDebounceTimeout()) //
                .onBlur(viewSettings.getOnBlur()) //
                .onChange(viewSettings.getOnChange()) //
                .onKeyDown(viewSettings.getOnKeyDown()) //
                .onToggle(viewSettings.getOnToggle()) //
                .addAllOnSelect(viewSettings.getOnSelect())
                .placeholder(viewSettings.getPlaceholder());
    }

    public static class Builder {

        private int debounceTimeout;

        private String placeholder;

        private String onBlur;

        private String onChange;

        private String onKeyDown;

        private String onToggle;

        private Map<String, String> onSelect = new LinkedHashMap<>();

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

        public Builder addAllOnSelect(final Map<String, String> onSelects) {
            this.onSelect.putAll(onSelects);
            return this;
        }

        public Builder translate() {
            this.placeholder = DataprepBundle.message(this.placeholder);
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
