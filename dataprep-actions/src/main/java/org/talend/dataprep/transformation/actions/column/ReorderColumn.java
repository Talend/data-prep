// ============================================================================
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

package org.talend.dataprep.transformation.actions.column;

import static java.util.Collections.singletonList;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.VALUES_COLUMN;
import static org.talend.dataprep.exception.error.ActionErrorCodes.UNEXPECTED_EXCEPTION;
import static org.talend.dataprep.transformation.actions.category.ActionScope.COLUMN_METADATA;
import static org.talend.dataprep.transformation.actions.category.ActionScope.HIDDEN_IN_ACTION_LIST;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.COLUMN_ID;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.SELECTED_COLUMN_PARAMETER;

import java.util.*;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.Quality;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.DataSetAction;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * This action reorder columns. The column will be move to the selected column. All other columns will be moved as well.
 */
@Action(ReorderColumn.REORDER_ACTION_NAME)
public class ReorderColumn extends AbstractActionMetadata implements DataSetAction {

    /**
     * The action name.
     */
    public static final String REORDER_ACTION_NAME = "reorder"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(ReorderColumn.class);

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return REORDER_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.COLUMNS.getDisplayName(locale);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        List<Parameter> parameters = super.getParameters(locale);
        parameters.add(Parameter.parameter(locale).setName(OtherColumnParameters.SELECTED_COLUMN_PARAMETER)
                .setType(ParameterType.COLUMN)
                .setDefaultValue(StringUtils.EMPTY)
                .setCanBeBlank(false)
                .build(this));
        return parameters;
    }

    @Override
    public List<String> getActionScope() {
        return Arrays.asList(COLUMN_METADATA.getDisplayName(), HIDDEN_IN_ACTION_LIST.getDisplayName());
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        // accept all types of columns
        return true;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(actionContext, singletonList(ActionsUtils.additionalColumn()));
        }

        Map<String, String> parameters = actionContext.getParameters();

        RowMetadata rowMetadata = actionContext.getRowMetadata();

        String targetColumnId = parameters.get(SELECTED_COLUMN_PARAMETER);

        ColumnMetadata targetColumn = rowMetadata.getById(targetColumnId);

        if (targetColumn == null) {
            return;
        }

        String originColumnId = parameters.get(COLUMN_ID.getKey());

        // column id may be different from index in the list
        // we cannot rely on id as index
        // so we have to find first the origin and target index
        int index = 0, originIndex = 0, targetIndex = 0;
        for (ColumnMetadata columnMetadata : rowMetadata.getColumns()) {
            if (StringUtils.equals(columnMetadata.getId(), originColumnId)) {
                originIndex = index;
            }
            if (StringUtils.equals(columnMetadata.getId(), targetColumnId)) {
                targetIndex = index;
            }
            index++;
        }

        // now we have both index so we can iterate again and swap few columns
        // we have different case as target can he lower than origin or the opposite
        boolean forwardMove = targetIndex > originIndex;

        try {
            if (forwardMove) {
                for (index = originIndex; index < targetIndex; index++) {
                    swapColumnMetadata(rowMetadata.getColumns().get(index), rowMetadata.getColumns().get(index + 1));
                }
            } else {
                for (index = originIndex; index > targetIndex; index--) {
                    swapColumnMetadata(rowMetadata.getColumns().get(index), rowMetadata.getColumns().get(index - 1));
                }
            }
        } catch (Exception e) {
            LOGGER.debug("cannot swap columns: {}", e.getMessage());
            throw new TalendRuntimeException(UNEXPECTED_EXCEPTION,
                    build().put("message", e.getMessage()));
        }
    }

    protected void swapColumnMetadata(ColumnMetadata originColumn, ColumnMetadata targetColumn) throws Exception {

        ColumnMetadata targetColumnCopy = ColumnMetadata.Builder.column().copy(targetColumn).build();
        ColumnMetadata originColumnCopy = ColumnMetadata.Builder.column().copy(originColumn).build();

        BeanUtils.copyProperties(targetColumn, originColumn);
        BeanUtils.copyProperties(originColumn, targetColumnCopy);

        Statistics originalStatistics = originColumnCopy.getStatistics();
        Statistics targetStatistics = targetColumnCopy.getStatistics();

        BeanUtils.copyProperties(targetColumn.getStatistics(), originalStatistics);
        BeanUtils.copyProperties(originColumn.getStatistics(), targetStatistics);

        Quality originalQuality = originColumnCopy.getQuality();
        Quality targetQualityCopty = targetColumnCopy.getQuality();

        BeanUtils.copyProperties(targetColumn.getQuality(), originalQuality);
        BeanUtils.copyProperties(originColumn.getQuality(), targetQualityCopty);

    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        // no op
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(VALUES_COLUMN);
    }

}
