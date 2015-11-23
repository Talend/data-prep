package org.talend.dataprep.transformation.api.action.parameters;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameter that should be displayed as a select box in the UI.
 */
public class SelectParameter extends Parameter implements Serializable {

    /** The select items. */
    @JsonIgnore // will be part of the Parameter#configuration
    private List<Item> items;

    /** True if multiple items can be selected. */
    @JsonIgnore // will be part of the Parameter#configuration
    private boolean multiple;

    /**
     * Private constructor to ensure the use of builder.
     *
     * @param name The parameter name.
     * @param defaultValue The parameter default value.
     * @param implicit True if the parameter is implicit.
     * @param canBeBlank True if the parameter can be blank.
     * @param items List of items for this select parameter.
     * @param multiple True if multiple selection is allowed.
     */
    private SelectParameter(String name, String defaultValue, boolean implicit, boolean canBeBlank, List<Item> items,
            boolean multiple) {
        super(name, ParameterType.SELECT, defaultValue, implicit, canBeBlank);
        setItems(items);
        setMultiple(multiple);
    }

    /**
     * @return the Items
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * @param items the items to set.
     */
    public void setItems(List<Item> items) {
        addConfiguration("values", items);
        this.items = items;
    }

    /**
     * @return the Multiple
     */
    public boolean isMultiple() {
        return multiple;
    }

    /**
     * @param multiple the multiple to set.
     */
    public void setMultiple(boolean multiple) {
        addConfiguration("multiple", multiple);
        this.multiple = multiple;
    }

    /**
     * Models a select item.
     */
    public static class Item {

        /** the item value. */
        private String value;

        /** The optional inline parameter. */
        @JsonProperty("parameters")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<Parameter> inlineParameters;

        /**
         * Create a select Item.
         *
         * @param value the item value.
         * @param parameters the item optional parameters.
         */
        public Item(String value, List<Parameter> parameters) {
            this.value = value;
            this.inlineParameters = parameters;
        }

        /**
         * Create a select Item.
         *
         * @param value the item value.
         */
        public Item(String value) {
            this(value, emptyList());
        }

        /**
         * @return the Value
         */
        public String getValue() {
            return value;
        }

        /**
         * @return the InlineParameters
         */
        public List<Parameter> getInlineParameters() {
            return inlineParameters;
        }
    }

    /**
     * Builder used to simplify the syntax of creation.
     */
    public static class Builder {

        /** The parameter name. */
        private String name = "";

        /** List of items. */
        private List<Item> items = new ArrayList<>();

        /** The parameter default value. */
        private String defaultValue = "";

        /** True if the parameter is not displayed to the user. */
        private boolean implicit = false;

        /** True if the parameter can be blank. */
        private boolean canBeBlank = false;

        /** True if the selection is multiple. */
        private boolean multiple = false;

        /**
         * @return A SelectParameter builder.
         */
        public static SelectParameter.Builder builder() {
            return new Builder();
        }

        /**
         * Set the name of the select parameter.
         *
         * @param name the name of the select parameter.
         * @return the builder to carry on building the column.
         */
        public SelectParameter.Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the defaultValue of the select parameter.
         *
         * @param defaultValue the default value of the select parameter.
         * @return the builder to carry on building the column.
         */
        public SelectParameter.Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Set the implicit of the select parameter.
         *
         * @param implicit true if the parameter is implicit.
         * @return the builder to carry on building the column.
         */
        public SelectParameter.Builder implicit(boolean implicit) {
            this.implicit = implicit;
            return this;
        }

        /**
         * Set the canBeBlank of the select parameter.
         *
         * @param canBeBlank true if the parameter is implicit.
         * @return the builder to carry on building the column.
         */
        public SelectParameter.Builder canBeBlank(boolean canBeBlank) {
            this.canBeBlank = canBeBlank;
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         * @param parameter the item optional parameter.
         * @return the builder to carry on building the column.
         */
        public SelectParameter.Builder item(String value, Parameter... parameter) {
            this.items.add(new Item(value, Arrays.asList(parameter)));
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         * @return the builder to carry on building the column.
         */
        public SelectParameter.Builder item(String value) {
            this.items.add(new Item(value, emptyList()));
            return this;
        }

        /**
         * Add all items to the select parameter builder.
         *
         * @param items the item name.
         * @return the builder to carry on building the column.
         */
        public SelectParameter.Builder items(List<Item> items) {
            this.items.addAll(items);
            return this;
        }

        /**
         * Build the column with the previously entered values.
         *
         * @return the built column metadata.
         */
        public SelectParameter build() {
            return new SelectParameter(name, defaultValue, implicit, canBeBlank, items, multiple);
        }

    }

}
