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

package org.talend.dataprep.transformation.api.action.metadata.column;

import static org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata.Behavior.VALUES_COLUMN;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.Quality;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.DataSetAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;

/**
 * This action reorder columns. The column will be move to the selected column. All other columns will be moved as well.
 */
@Component(ReorderColumn.ACTION_BEAN_PREFIX + ReorderColumn.REORDER_ACTION_NAME)
public class ReorderColumn extends ActionMetadata implements DataSetAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReorderColumn.class);

    /**
     * The action name.
     */
    public static final String REORDER_ACTION_NAME = "reorder"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return REORDER_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.COLUMNS.getDisplayName();
    }

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, StringUtils.EMPTY,
                false, false));
        return parameters;
    }

    @Override
    public List<String> getActionScope() {
        return Collections.emptyList();
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        // accept all types of columns
        return true;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);

        Map<String, String> parameters = actionContext.getParameters();

        RowMetadata rowMetadata = actionContext.getRowMetadata();

        String targetColumnId = parameters.get(OtherColumnParameters.SELECTED_COLUMN_PARAMETER);

        ColumnMetadata targetColumn = rowMetadata.getById(targetColumnId);

        if (targetColumn == null) {
            return;
        }

        String originColumnId = parameters.get(ImplicitParameters.COLUMN_ID.getKey());

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
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, ExceptionContext.build().put("message", e.getMessage()));
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
