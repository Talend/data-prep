// ============================================================================
//
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

package org.talend.dataprep.api.service.settings.views.api.list;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * List display menu settings, placed on the toolbar
 */
@JsonInclude(NON_NULL)
public class ListDisplaySettings {
    /**
     * The option select action identifier
     */
    private String onChange;

    public String getOnChange() {
        return onChange;
    }

    public void setOnChange(final String onChange) {
        this.onChange = onChange;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static final String ID = "id";

        private static final String NAME = "name";

        private String onChange;

        public Builder onChange(final String onChange) {
            this.onChange = onChange;
            return this;
        }

        public ListDisplaySettings build() {
            final ListDisplaySettings settings = new ListDisplaySettings();
            settings.setOnChange(this.onChange);
            return settings;
        }
    }
}
