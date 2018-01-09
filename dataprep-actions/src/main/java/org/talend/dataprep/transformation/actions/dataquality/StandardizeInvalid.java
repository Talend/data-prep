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

package org.talend.dataprep.transformation.actions.dataquality;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.model.CategoryType;
import org.talend.dataquality.semantic.model.DQCategory;

import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

/**
 * Find a closest valid value from a dictionary.
 */
@Action(StandardizeInvalid.ACTION_NAME)
public class StandardizeInvalid extends AbstractActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
    public static final String ACTION_NAME = "standardize_value";

    /**
     * parameter of matching threshold
     */
    private static final String MATCH_THRESHOLD_PARAMETER = "match_threshold";

    /**
     * User selected match threshold
     */
    private static final String MATCH_THRESHOLD_KEY = "match_threshold_key";

    /**
     * The selected column if it uses semantic category
     */
    private static final String COLUMN_IS_SEMANTIC_KEY = "is_semantic_category";

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardizeInvalid.class);

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        Parameter startParameter = selectParameter(locale)
                .name(MATCH_THRESHOLD_PARAMETER)
                .item(MatchThresholdEnum.HIGH.name(), MatchThresholdEnum.HIGH.getLabel())
                .item(MatchThresholdEnum.DEFAULT.name(), MatchThresholdEnum.DEFAULT.getLabel())
                .item(MatchThresholdEnum.NONE.name(), MatchThresholdEnum.NONE.getLabel())
                .defaultValue(MatchThresholdEnum.DEFAULT.name())
                .build(this);
        parameters.add(startParameter);
        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(actionContext, singletonList(ActionsUtils.additionalColumn()));
        }
        if (actionContext.getActionStatus() == OK) {
            Map<String, String> parameters = actionContext.getParameters();
            Optional<Double> thresholdValue = getThresholdFromParameters(parameters);
            if (thresholdValue.isPresent()) {
                actionContext.get(MATCH_THRESHOLD_KEY, p -> thresholdValue.get());
                // this action only apply for column uses Semantic category.
                final RowMetadata rowMetadata = actionContext.getRowMetadata();
                final String columnId = actionContext.getColumnId();
                final ColumnMetadata column = rowMetadata.getById(columnId);
                actionContext.get(COLUMN_IS_SEMANTIC_KEY, p -> isDictionaryType(column));
            } else {
                LOGGER.warn("No valid threshold value received: got {}.", parameters.get(MATCH_THRESHOLD_PARAMETER));
                actionContext.setActionStatus(CANCELED);
            }
        }
    }

    private Optional<Double> getThresholdFromParameters(Map<String, String> parameters) {
        Optional<Double> thresholdValue;
        String matchThresholdPara = parameters.get(MATCH_THRESHOLD_PARAMETER);
        if (StringUtils.isNotBlank(matchThresholdPara)) {
            try {
                thresholdValue = Optional.of(MatchThresholdEnum.valueOf(matchThresholdPara).getThreshold());
            } catch (IllegalArgumentException e) {
                thresholdValue = empty();
            }
        } else {
            thresholdValue = empty();
        }
        return thresholdValue;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.DATA_CLEANSING.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return isDictionaryType(column);
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        if (isApplicable(row, context)) {
            final String columnId = context.getColumnId();
            final String value = row.get(columnId);
            final RowMetadata rowMetadata = context.getRowMetadata();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            final Double threshold = context.get(MATCH_THRESHOLD_KEY);
            String closestValue =
                    CategoryRegistryManager.getInstance().findMostSimilarValue(value, column.getDomain(), threshold);
            // If not found the similar value, display original value.
            if (!StringUtils.isEmpty(closestValue)) {
                row.set(columnId, closestValue);
            }
        }
    }

    /**
     * Applicable row only when the column uses semantic and the row value is invalid.
     */
    private boolean isApplicable(DataSetRow row, ActionContext context) {
        boolean isColumnUseSemantic = context.get(COLUMN_IS_SEMANTIC_KEY);
        if (isColumnUseSemantic) {
            final String columnId = context.getColumnId();
            final String value = row.get(columnId);
            return !StringUtils.isEmpty(value) && row.isInvalid(columnId);
        }
        return false;
    }

    private boolean isDictionaryType(ColumnMetadata column) {
        String domain = column.getDomain();
        if (!StringUtils.isEmpty(domain)) {
            DQCategory category = CategoryRegistryManager.getInstance().getCategoryMetadataByName(domain);
            return category != null && CategoryType.DICT.equals(category.getType());
        }
        return false;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN, Behavior.NEED_STATISTICS_INVALID);
    }

    private enum MatchThresholdEnum {
        HIGH("high_match", 0.9),
        DEFAULT("default_match", 0.8),
        NONE("none_match", 0.0);

        private final String label;

        private final Double threshold;

        MatchThresholdEnum(String label, Double threshold) {
            this.label = label;
            this.threshold = threshold;
        }

        public Double getThreshold() {
            return threshold;
        }

        public String getLabel() {
            return label;
        }
    }
}
