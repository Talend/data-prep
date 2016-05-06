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

package org.talend.dataprep.transformation.api.action.metadata.datablending;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters.COLUMN_ID;
import static org.talend.dataprep.transformation.api.action.metadata.datablending.Lookup.Parameters.*;
import static org.talend.dataprep.parameters.ParameterType.LIST;
import static org.talend.dataprep.parameters.ParameterType.STRING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.DataSetAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.parameters.Parameter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lookup action used to blend a (or a part of a) dataset into another one.
 */
@Component(Lookup.ACTION_BEAN_PREFIX + Lookup.LOOKUP_ACTION_NAME)
@Scope("prototype")
public class Lookup extends ActionMetadata implements DataSetAction {

    /** The action name. */
    public static final String LOOKUP_ACTION_NAME = "lookup"; //$NON-NLS-1$

    /** This class' logger. */
    public static final Logger LOGGER = LoggerFactory.getLogger(Lookup.class);

    /** DataSet service url. */
    @Value("${dataset.service.url}")
    private String datasetServiceUrl;

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

    /** Spring application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The dataprep ready jackson builder. */
    @Autowired
    @Lazy // needed to prevent a circular dependency
    private ObjectMapper mapper;

    /** Adapted value of the name parameter. */
    private String adaptedNameValue = EMPTY;

    /** Adapted value of the dataset_id parameter. */
    private String adaptedDatasetIdValue = EMPTY;

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
    public String getCategory() {
        return ActionCategory.DATA_BLENDING.getDisplayName();
    }

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = new ArrayList<>();
        parameters.add(ImplicitParameters.COLUMN_ID.getParameter());
        parameters.add(ImplicitParameters.FILTER.getParameter());
        parameters.add(new Parameter(LOOKUP_DS_NAME.getKey(), STRING, adaptedNameValue, false, false, StringUtils.EMPTY , getMessagesBundle()));
        parameters.add(new Parameter(LOOKUP_DS_ID.getKey(), STRING, adaptedDatasetIdValue, false, false, StringUtils.EMPTY, getMessagesBundle()));
        parameters.add(new Parameter(LOOKUP_JOIN_ON.getKey(), STRING, EMPTY, false, false, StringUtils.EMPTY, getMessagesBundle()));
        parameters.add(new Parameter(LOOKUP_JOIN_ON_NAME.getKey(), STRING, EMPTY, false, false, StringUtils.EMPTY, getMessagesBundle()));
        parameters.add(new Parameter(LOOKUP_SELECTED_COLS.getKey(), LIST, EMPTY, false, false, StringUtils.EMPTY, getMessagesBundle()));
        return parameters;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
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
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            List<LookupSelectedColumnParameter> colsToAdd = getColsToAdd(context.getParameters());
            if (colsToAdd.isEmpty()) {
                context.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }
            //
            LookupRowMatcher rowMatcher = context.get("rowMatcher", //
                        (p) -> {
                       String dataSetId = p.get(LOOKUP_DS_ID.getKey());
                       return applicationContext.getBean(LookupRowMatcher.class, dataSetId);
            });
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
                    final ColumnMetadata colMetadata = ColumnMetadata.Builder //
                            .column() //
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
        try {
            final String cols = parameters.get(LOOKUP_SELECTED_COLS.getKey());
            return mapper.readValue(cols, new TypeReference<List<LookupSelectedColumnParameter>>() {
            });
        } catch (IOException e) {
            LOGGER.debug("Unable to parse parameter.", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
