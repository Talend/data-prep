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

package org.talend.dataprep.transformation.api.action.metadata.line;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.parameters.ParameterType.BOOLEAN;

import java.util.*;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.metadata.common.RowAction;
import org.talend.dataprep.parameters.Parameter;

/**
 * This action does two things:
 * <ul>
 * <li>Take the value in each column of this row, and use them as column names</li>
 * <li>Delete this row</li>
 * </ul>
 */
@Component(MakeLineHeader.ACTION_BEAN_PREFIX + MakeLineHeader.ACTION_NAME)
public class MakeLineHeader extends ActionMetadata implements RowAction {

    public static final String ACTION_NAME = "make_line_header";

    public static final String SKIP_UNTIL = "make_line_header_skip_until";

    private static final Logger LOGGER = LoggerFactory.getLogger(MakeLineHeader.class);

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    @Override
    protected boolean implicitFilter() {
        return false;
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.singletonList(new Parameter(SKIP_UNTIL, BOOLEAN, Boolean.TRUE.toString()));
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
            return;
        }

        if (context.getFilter().test(row)) {
            LOGGER.debug("Make line header for rowId {} with parameters {} ", context.getRowId(), context.getParameters());
            for (ColumnMetadata column : context.getRowMetadata().getColumns()) {
                String newColumnName = context.get(column.getId(), p -> row.get(column.getId()));
                column.setName(newColumnName);
            }
            row.setDeleted(true);
        } else {
            for (ColumnMetadata column : context.getRowMetadata().getColumns()) {
                if (!context.has(column.getId())) {
                    // Action hasn't yet found new headers
                    break;
                }
                String newColumnName = context.get(column.getId());
                column.setName(newColumnName);
            }
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CHANGE_NAME, Behavior.VALUES_ALL);
    }

}
