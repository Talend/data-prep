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

package org.talend.dataprep.transformation.actions.datablending;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.parameters.ParameterType.LIST;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.transformation.actions.Providers.get;
import static org.talend.dataprep.transformation.actions.category.ActionScope.HIDDEN_IN_ACTION_LIST;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.COLUMN_ID;
import static org.talend.dataprep.transformation.actions.datablending.Lookup.Parameters.*;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.DataSetAction;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lookup action used to blend a (or a part of a) dataset into another one.
 */
@Action(Lookup.LOOKUP_ACTION_NAME)
public class Lookup extends AbstractActionMetadata implements DataSetAction {

    /** The action name. */
    public static final String LOOKUP_ACTION_NAME = "lookup"; //$NON-NLS-1$

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Lookup.class);

    /** Adapted value of the name parameter. */
    private String adaptedNameValue = EMPTY;

    /** Adapted value of the dataset_id parameter. */
    private String adaptedDatasetIdValue = EMPTY;

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    /**
     * @return A unique name used to identify action.
     */
    @Override
    public String getName() {
        return LOOKUP_ACTION_NAME;
    }

    /**
     * @return A 'category' for the action used to group similar actions (eg. 'math', 'repair'...).
     */
    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.DATA_BLENDING.getDisplayName(locale);
    }

    @Override
    public List<String> getActionScope() {
        return Collections.singletonList(HIDDEN_IN_ACTION_LIST.getDisplayName());
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = new ArrayList<>();
        parameters.add(ImplicitParameters.COLUMN_ID.getParameter(locale));
        parameters.add(ImplicitParameters.FILTER.getParameter(locale));
        parameters.add(Parameter.parameter(locale).setName(LOOKUP_DS_NAME.getKey())
                .setType(STRING)
                .setDefaultValue(adaptedNameValue)
                .setCanBeBlank(false)
                .build(this));
        parameters.add(Parameter.parameter(locale).setName(LOOKUP_DS_ID.getKey())
                .setType(STRING)
                .setDefaultValue(adaptedDatasetIdValue)
                .setCanBeBlank(false)
                .build(this));
        parameters.add(Parameter.parameter(locale).setName(LOOKUP_JOIN_ON.getKey())
                .setType(STRING)
                .setDefaultValue(EMPTY)
                .setCanBeBlank(false)
                .build(this));
        parameters.add(Parameter.parameter(locale).setName(LOOKUP_JOIN_ON_NAME.getKey())
                .setType(STRING)
                .setDefaultValue(EMPTY)
                .setCanBeBlank(false)
                .build(this));
        parameters.add(Parameter.parameter(locale).setName(LOOKUP_SELECTED_COLS.getKey())
                .setType(LIST)
                .setDefaultValue(EMPTY)
                .setCanBeBlank(false)
                .build(this));
        return parameters;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        // because this is a specific action, suggestion will be handled by the API. Hence, default value is false.
        return false;
    }

    /**
     * Adapt the parameters default values according to the given dataset.
     *
     * @param dataset the dataset to adapt the parameters value from.
     * @return the adapted lookup
     */
    public Lookup adapt(DataSetMetadata dataset) {
        adaptedNameValue = dataset.getName();
        adaptedDatasetIdValue = dataset.getId();
        return this;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, singletonList(ActionsUtils.additionalColumn()));
        }
        if (context.getActionStatus() == OK) {
            List<LookupSelectedColumnParameter> colsToAdd = getColsToAdd(context.getParameters());
            if (colsToAdd.isEmpty()) {
                context.setActionStatus(CANCELED);
            }

            LookupRowMatcher rowMatcher = context.get("rowMatcher", p -> get(LookupRowMatcher.class, p));
            // Create lookup result columns
            final Map<String, String> parameters = context.getParameters();
            final String columnId = parameters.get(COLUMN_ID.getKey());
            final RowMetadata lookupRowMetadata = rowMatcher.getRowMetadata();
            final RowMetadata rowMetadata = context.getRowMetadata();
            colsToAdd.forEach(toAdd -> {
                // create the new column
                final String toAddColumnId = toAdd.getId();
                final ColumnMetadata metadata = lookupRowMetadata.getById(toAddColumnId);
                context.column(toAddColumnId, r -> {
                    final ColumnMetadata colMetadata = //
                            column() //
                            .copy(metadata) //
                            .computedId(null) // id should be set by the insertAfter method
                            .build();
                    rowMetadata.insertAfter(columnId, colMetadata);
                    return colMetadata;
                });
            });
        }
    }

    /**
     * @see DataSetAction#applyOnDataSet(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {

        // read parameters
        final Map<String, String> parameters = context.getParameters();
        String columnId = parameters.get(COLUMN_ID.getKey());
        String joinValue = row.get(columnId);
        String joinOn = parameters.get(LOOKUP_JOIN_ON.getKey());

        // get the rowMatcher from context
        LookupRowMatcher rowMatcher = context.get("rowMatcher");

        // get the matching lookup row
        DataSetRow matchingRow = rowMatcher.getMatchingRow(joinOn, joinValue);

        LOGGER.trace("For "+ joinValue+" I have found this matching row: "+matchingRow.values().values());

        // get the columns to add
        List<LookupSelectedColumnParameter> colsToAdd = getColsToAdd(parameters);
        colsToAdd.forEach(toAdd -> {
            // get the new column
            String newColId = context.column(toAdd.getId());
            // insert new row value
            row.set(newColId, matchingRow.get(toAdd.getId()));
        });
    }

    /**
     * Return the list of columns to merge in the result from the parameters.
     *
     * @param parameters the action parameters.
     * @return the list of columns to merge.
     */
    private List<LookupSelectedColumnParameter> getColsToAdd(Map<String, String> parameters) {
        List<LookupSelectedColumnParameter> result;
        try {
            final String cols = parameters.get(LOOKUP_SELECTED_COLS.getKey());
            result =  new ObjectMapper().readValue(cols, new TypeReference<List<LookupSelectedColumnParameter>>() {
            });
        } catch (IOException e) {
            LOGGER.debug("Unable to parse parameter.", e);
            result = Collections.emptyList();
        }
        return result;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

    /** Lookup parameters */
    public enum Parameters {
        LOOKUP_DS_NAME,
        LOOKUP_DS_ID,
        LOOKUP_JOIN_ON,
        LOOKUP_JOIN_ON_NAME, // needed to display human friendly parameters
        LOOKUP_SELECTED_COLS;

        /** Return a human readable key. */
        public String getKey() {
            return this.name().toLowerCase();
        }
    }

}
