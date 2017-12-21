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

package org.talend.dataprep.transformation.actions.datamasking;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.talend.dataprep.api.type.Type.DATE;
import static org.talend.dataprep.api.type.Type.get;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.datamasking.semantic.ValueDataMasker;

/**
 * Mask sensitive data according to the semantic category.
 */
@Action(MaskDataByDomain.ACTION_NAME)
public class MaskDataByDomain extends AbstractActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
    public static final String ACTION_NAME = "mask_data_by_domain"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(MaskDataByDomain.class);
    /**
     * Key for storing in ActionContext:
     */
    private static final String MASKER = "masker"; //$NON-NLS-1$

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.DATA_MASKING.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.isAssignableFrom(Type.get(column.getType()));
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        if (StringUtils.isNotBlank(value)) {
            try {
                final ValueDataMasker masker = context.get(MASKER);
                row.set(columnId, masker.maskValue(value));
            } catch (Exception e) {
                // Nothing to do, we let the original value as is
                LOGGER.debug("Unable to process value '{}'.", value, e);
            }
        }
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(actionContext, singletonList(ActionsUtils.additionalColumn()));
        }
        if (actionContext.getActionStatus() == OK) {
            final RowMetadata rowMetadata = actionContext.getRowMetadata();
            final String columnId = actionContext.getColumnId();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            final String domain = column.getDomain();
            final Type type = get(column.getType());
            LOGGER.trace(">>> type: " + type + " metadata: " + column);
            try {
                if (DATE.equals(type)) {
                    final List<PatternFrequency> patternFreqList = column.getStatistics().getPatternFrequencies();
                    final List<String> dateTimePatternList = patternFreqList.stream() //
                            .map(PatternFrequency::getPattern) //
                            .collect(toList());
                    actionContext.get(MASKER, p -> new ValueDataMasker(domain, type.getName(), dateTimePatternList));
                } else {
                    actionContext.get(MASKER, p -> new ValueDataMasker(domain, type.getName()));
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                actionContext.setActionStatus(CANCELED);
            }
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN, Behavior.NEED_STATISTICS_INVALID);
    }

}
