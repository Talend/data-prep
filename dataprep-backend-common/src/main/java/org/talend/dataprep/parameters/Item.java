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

package org.talend.dataprep.parameters;

import static java.util.Collections.emptyList;

import java.util.List;

/**
 * Models a select item.
 */
public interface Item {

    String getValue();

    String getLabel();

    List<Parameter> getParameters();

    Item attach(Object parent);

    class Builder {

        private String label;

        private String value;

        private List<Parameter> inlineParameters;

        private String text;

        public static Builder builder() {
            return new Builder();
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder inlineParameters(List<Parameter> inlineParameters) {
            this.inlineParameters = inlineParameters;
            return this;
        }

        public Item build() {
            final List<Parameter> parameters = inlineParameters == null ? emptyList() : inlineParameters;
            if (label == null) {
                if (text == null) {
                    return new TextItem(value, parameters);
                } else {
                    return new TextItem(value, text, parameters);
                }
            } else {
                return new LocalizedItem(value, label, parameters);
            }
        }

    }

}
