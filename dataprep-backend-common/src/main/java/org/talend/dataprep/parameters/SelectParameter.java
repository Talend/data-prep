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

package org.talend.dataprep.parameters;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.talend.dataprep.i18n.ActionsBundle.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Parameter that should be displayed as a select box in the UI.
 */
public class SelectParameter extends Parameter {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    // @JsonProperty
    private boolean radio;

    /** The select items. */
    @JsonIgnore // will be part of the Parameter#configuration
    private List<Item> items;

    /** True if multiple items can be selected. */
    @JsonIgnore // will be part of the Parameter#configuration
    private boolean multiple;

    // Introducing the dummy constructor for serialization purpose
    public SelectParameter() {
    }

    /**
     * Private constructor to ensure the use of builder.
     *
     * @param name The parameter name.
     * @param defaultValue The parameter default value.
     * @param implicit True if the parameter is implicit.
     * @param canBeBlank True if the parameter can be blank.
     * @param items List of items for this select parameter.
     * @param multiple True if multiple selection is allowed.
     * @param radio <code>true</code> if the rendering code should prefer radio buttons instead of drop down list.
     */
    private SelectParameter(String name, String defaultValue, boolean implicit, boolean canBeBlank, List<Item> items,
            boolean multiple, boolean radio, String label, String description) {
        super(name, ParameterType.SELECT, defaultValue, implicit, canBeBlank, EMPTY, label, description, true);
        setRadio(radio);
        setItems(items);
        setMultiple(multiple);
    }

    public boolean getRadio() {
        return radio;
    }

    public void setRadio(boolean radio) {
        this.radio = radio;
    }

    /**
     * @return A SelectParameter builder.
     * @param locale
     */
    public static SelectParameterBuilder selectParameter(Locale locale) {
        return new SelectParameterBuilder(locale);
    }

    public boolean isRadio() {
        return radio;
    }

    public List<Item> getItems() {
        return items;
    }

    private void setItems(List<Item> items) {
        addConfiguration("values", items);
        this.items = items;
    }

    public boolean isMultiple() {
        return multiple;
    }

    private void setMultiple(boolean multiple) {
        addConfiguration("multiple", multiple);
        this.multiple = multiple;
    }

    /**
     * Builder used to simplify the syntax of creation.
     */
    public static class SelectParameterBuilder {

        /** List of items. */
        private final List<Item> items = new ArrayList<>();

        /** True if the selection is multiple. */
        private final boolean multiple = false;

        /** The parameter name. */
        private String name = "";

        /** The parameter default value. */
        private String defaultValue = "";

        /** True if the parameter is not displayed to the user. */
        private boolean implicit;

        /** True if the parameter can be blank. */
        private boolean canBeBlank;

        /** True if rendering should prefer radio buttons to render parameters choices */
        private boolean radio;

        private String label;

        private String description;

        private Locale locale;

        public SelectParameterBuilder(Locale locale) {
            this.locale = locale;
        }

        /**
         * Set the name of the select parameter.
         *
         * @param name the name of the select parameter.
         * @return the builder to carry on building the selector.
         */
        public SelectParameterBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the defaultValue of the select parameter.
         *
         * @param defaultValue the default value of the select parameter.
         * @return the builder to carry on building the selector.
         */
        public SelectParameterBuilder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Set the implicit of the select parameter.
         *
         * @param implicit true if the parameter is implicit.
         * @return the builder to carry on building the selector.
         */
        public SelectParameterBuilder implicit(boolean implicit) {
            this.implicit = implicit;
            return this;
        }

        /**
         * Set the canBeBlank of the select parameter.
         *
         * @param canBeBlank true if the parameter is implicit.
         * @return the builder to carry on building the selector.
         */
        public SelectParameterBuilder canBeBlank(boolean canBeBlank) {
            this.canBeBlank = canBeBlank;
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         * @param parameter the item optional parameter.
         * @return the builder to carry on building the selector.
         */
        public SelectParameterBuilder item(String value, Parameter... parameter) {
            this.items.add(new Item(value, value, Arrays.asList(parameter)));
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         */
        public SelectParameterBuilder item(String value) {
            this.items.add(new Item(value, value, null));
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         * @param labelKey the key of the item label. The item's label will be by default looked up with key ("choice." + value).
         * @return the builder to carry on building the selector.
         */
        public SelectParameterBuilder item(String value, String labelKey) {
            this.items.add(new Item(value, choice(null, locale, labelKey),  null));
            return this;
        }

        /**
         * Add a 'constant' item (an item with a value, but no label translation) to the select parameter builder. Unlike the
         * {@link #item(String, String)} the second parameter
         * is <b>not</b> a key to a i18n label but a constant label to be taken as is.
         *
         * @param value the item value.
         * @param text the item (constant) label
         * @return the builder to carry on building the selector.
         */
        public SelectParameterBuilder constant(String value, String text) {
            this.items.add(new Item(value, text, null));
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         * @param labelKey the key of the item label. The item's label will be by default looked up with key ("choice." + value).
         * @param parameter the item optional parameter.
         * @return the builder to carry on building the selector.
         */
        public SelectParameterBuilder item(String value, String labelKey, Parameter... parameter) {
            this.items.add(new Item(value, choice(null, locale, labelKey), Arrays.asList(parameter)));
            return this;
        }

        /**
         * Add all items to the select parameter builder.
         *
         * @param items the item name.
         * @return the builder to carry on building the selector.
         */
        public SelectParameterBuilder items(List<Item> items) {
            this.items.addAll(items);
            return this;
        }

        public SelectParameterBuilder radio(boolean radio) {
            this.radio = radio;
            return this;
        }

        public SelectParameterBuilder setLabel(String label) {
            this.label = label;
            return this;
        }

        public SelectParameterBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Build the column with the previously entered values.
         *
         * @return the built column metadata.
         * @param action
         */
        public SelectParameter build(Object action) {
            if (label == null) {
                label = parameterLabel(action, locale, name);
            }
            if (description == null) {
                description = parameterDescription(action, locale, name);
            }
            return new SelectParameter(name, defaultValue, implicit, canBeBlank, items, multiple, radio, label, description);
        }
    }

}
