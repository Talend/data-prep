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

package org.talend.dataprep.transformation.actions.line;

import static org.talend.dataprep.parameters.ParameterType.BOOLEAN;
import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;

import java.text.MessageFormat;
import java.util.*;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * This action does two things:
 * <ul>
 * <li>Take the value in each column of this row, and use them as column names</li>
 * <li>Delete this row</li>
 * </ul>
 */
@Action(MakeLineHeader.ACTION_NAME)
public class MakeLineHeader extends AbstractActionMetadata implements RowAction {

    public static final String ACTION_NAME = "make_line_header";

    public static final String SKIP_UNTIL = "make_line_header_skip_until";

    private static final Logger LOGGER = LoggerFactory.getLogger(MakeLineHeader.class);

    private static final String DEFAULT_TITLE_KEY = "DEFAULT_TITLE_KEY";

    private static final String DEFAULT_TITLE_VALUE_MASK = "Col {0}";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return DATA_CLEANSING.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public boolean implicitFilter() {
        return false;
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        return Collections.singletonList(Parameter.parameter(locale).setName(SKIP_UNTIL)
                .setType(BOOLEAN)
                .setDefaultValue(true)
                .build(this));
    }

    @Override
    public void applyOnLine(DataSetRow row, ActionContext context) {
        Map<String, String> parameters = context.getParameters();
        String skipUntilStr = parameters.get(SKIP_UNTIL);
        // default is true
        boolean skipPreviousRows = StringUtils.isBlank(skipUntilStr) || BooleanUtils.toBoolean(skipUntilStr);

        long tdpId = row.getTdpId();
        long rowId = NumberUtils.toLong(parameters.get(ImplicitParameters.ROW_ID.getKey()), 0);

        if (skipPreviousRows && (tdpId < rowId)) {
            row.setDeleted(true);
        } else if (context.getFilter().test(row)) {
            setHeadersFromRow(row, context);
        } else {
            setRemainingRowColumnsNames(context);
        }
    }

    private void setRemainingRowColumnsNames(ActionContext context) {
        for (ColumnMetadata column : context.getRowMetadata().getColumns()) {
            if (!context.has(column.getId())) {
                // Action hasn't yet found new headers
                break;
            }
            String newColumnName = context.get(column.getId());
            column.setName(newColumnName);
        }
    }

    private void setHeadersFromRow(DataSetRow row, ActionContext context) {
        LOGGER.debug("Make line header for rowId {} with parameters {} ", context.getRowId(), context.getParameters());
        List<ColumnMetadata> columns = context.getRowMetadata().getColumns();
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            ColumnMetadata column = columns.get(columnIndex);
            // get new column name keyed on column id from context or use the row value
            final int columnViewIndex = columnIndex + 1;
            String newColumnName = context.get(column.getId(), p -> {
                String name = row.get(column.getId());
                if (StringUtils.isBlank(name)) {
                    MessageFormat pattern = context.get(DEFAULT_TITLE_KEY, q -> new MessageFormat(DEFAULT_TITLE_VALUE_MASK));
                    name = pattern.format(new Object[]{columnViewIndex});
                }
                return name;
            });
            column.setName(newColumnName);
        }
        row.setDeleted(true);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CHANGE_NAME, Behavior.VALUES_ALL, Behavior.FORBID_DISTRIBUTED, Behavior.VALUES_DELETE_ROWS);
    }

}
