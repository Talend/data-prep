/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.transformation.actions.common;

import static org.talend.dataprep.parameters.ParameterType.BOOLEAN;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

public class ActionsUtils {

    /**
     * Key for the "Create new column" parameter.
     *
     * @see org.talend.dataprep.transformation.pipeline.builder.ActionsStaticProfiler#CREATE_NEW_COLUMN
     */
    public static final String CREATE_NEW_COLUMN = "create_new_column";

    /**
     * Key for the context map to retrieve column created by "Create new column" parameter.
     */
    private static final String TARGET_COLUMN_CONTEXT_KEY = "target";

    private ActionsUtils() {
    }

    // For TDP-3798, add a checkbox for most actions to allow the user to choose if action is applied in place or if it
    // creates a new column:
    public static Parameter getColumnCreationParameter(Locale locale, boolean createNewColumnDefault) {
        return Parameter.parameter(locale).setName(CREATE_NEW_COLUMN)
                .setType(BOOLEAN)
                .setDefaultValue(createNewColumnDefault)
                .setCanBeBlank(false)
                .setImplicit(false)
                .build(null);
    }

    /**
     * Utility method to add the column creation parameter in the supplied list.
     *
     * @return the supplied parameter list with the column creation parameter
     */
    public static List<Parameter> appendColumnCreationParameter(List<Parameter> parameters, Locale locale, boolean createNewColumnDefault) {
        parameters.add(getColumnCreationParameter(locale, createNewColumnDefault));
        return parameters;
    }

    /**
     * Used by compile(ActionContext actionContext), evaluate if a new column needs to be created, if yes creates one.
     *
     * Actions that creates more than one column ('split', 'extract email parts', etc...) should manage this on their own.
     */
    public static void createNewColumn(ActionContext context, List<AdditionalColumn> additionalColumns) {
        String columnId = context.getColumnId();
        RowMetadata rowMetadata = context.getRowMetadata();

        context.get(TARGET_COLUMN_CONTEXT_KEY, r -> {
            final Map<String, String> cols = new HashMap<>();
            String nextId = columnId; // id of the column to put the new one after, initially the current column
            for (AdditionalColumn additionalColumn : additionalColumns) {
                    ColumnMetadata.Builder brandNewColumnBuilder = ColumnMetadata.Builder.column();

                    if (additionalColumn.getCopyMetadataFromId() != null) {
                        ColumnMetadata newColumn = context.getRowMetadata().getById(additionalColumn.getCopyMetadataFromId());
                        brandNewColumnBuilder.copy(newColumn).computedId(StringUtils.EMPTY);
                    }
                    brandNewColumnBuilder.name(additionalColumn.getName()) //
                     .type(additionalColumn.getType()); //

                ColumnMetadata columnMetadata = brandNewColumnBuilder.build();
                rowMetadata.insertAfter(nextId, columnMetadata);
                nextId = columnMetadata.getId(); // the new column to put next one after, is the fresh new one
                cols.put(additionalColumn.getKey(), columnMetadata.getId());
            }
            return cols;
        });
    }

    /**
     * Helper to retrieve the target column Id stored in the context.
     *
     * It can be the current column id if the function applies in place or id of the new column if the function creates one.
     * Must not be used for function that creates many columns. Use getTargetColumnIds(ActionContext context) instead in this case.
     *
     * @param context the action context
     * @return the target column ID
     */
    public static String getTargetColumnId(ActionContext context) {
        if (context.contains(TARGET_COLUMN_CONTEXT_KEY)) {
            Map<String, String> newColumns = context.get(TARGET_COLUMN_CONTEXT_KEY);
            return newColumns.values().iterator().next();
        } else {
            return context.getColumnId();
        }
    }

    /**
     * Returns new columns created by the function in case of it creates multiple ones. Like 'Split' or 'ExtractDateTokens'.
     *
     * @return a map in which keys are the 'key' from AdditionalColumn bean, and values columns ids.
     */
    public static Map<String, String> getTargetColumnIds(ActionContext context) {
        return context.get(TARGET_COLUMN_CONTEXT_KEY);
    }

    /**
     * For TDP-3798, add a checkbox for most actions to allow the user to choose if action is applied in place or if it
     * creates a new column.
     * This method is used by framework to evaluate if this step (action+parameters) creates a new column or is applied in place.
     *
     * For most actions, the default implementation is ok, but some actions (like 'split' that always creates new column) may
     * override it. In this case, no need to override createNewColumnParamVisible() and true.
     *
     * @return 'true' if this step (action+parameters) creates a new column, 'false' if it's applied in-place.
     */
    public static boolean doesCreateNewColumn(Map<String, String> parameters, boolean createNewColumnDefaultValue) {
        if (parameters.containsKey(CREATE_NEW_COLUMN)) {
            return Boolean.parseBoolean(parameters.get(CREATE_NEW_COLUMN));
        }
        return createNewColumnDefaultValue;
    }

    public static AdditionalColumn additionalColumn() {
        return new AdditionalColumn();
    }

    /**
     * Bean used to described all columns that can be created by a function.
     *
     * This will be used by createNewColumn(ActionContext context) to create all the new columns.
     */
    public static final class AdditionalColumn {

        /** Key to use in parameters map. */
        private String key;

        /** Name of the new colum. */
        private String name;

        /** Data base type of the new column. */
        private Type type = Type.STRING;

        /** Id of the column to copy metadata from */
        private String copyMetadataFromId;

        public String getKey() {
            return key;
        }

        public AdditionalColumn withKey(String key) {
            this.key = key;
            return this;
        }

        public String getName() {
            return name;
        }

        public AdditionalColumn withName(String name) {
            this.name = name;
            return this;
        }

        public Type getType() {
            return type;
        }

        public AdditionalColumn withType(Type type) {
            this.type = type;
            return this;
        }

        public String getCopyMetadataFromId() {
            return copyMetadataFromId;
        }

        public AdditionalColumn withCopyMetadataFromId(String from) {
            this.copyMetadataFromId = from;
            return this;
        }

    }
}
