// ============================================================================
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

package org.talend.dataprep.transformation.actions.common;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.api.filter.PolyglotFilterService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

public class CompileDataSetRowAction implements DataSetRowAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompileDataSetRowAction.class);

    private final Map<String, String> parametersCopy;

    private final ActionDefinition metadata;

    private final ScopeCategory scope;

    public CompileDataSetRowAction(Map<String, String> parametersCopy, ActionDefinition metadata,
                            ScopeCategory scope) {
        this.parametersCopy = parametersCopy;
        this.metadata = metadata;
        this.scope = scope;
    }

    @Override
    public DataSetRow apply(DataSetRow dataSetRow, ActionContext context) {
        return dataSetRow;
    }

    @Override
    public void compile(ActionContext actionContext) {
        try {
            actionContext.setParameters(parametersCopy);
            metadata.compile(actionContext);
            actionContext.setFilter(getFilter(parametersCopy, actionContext.getRowMetadata()));
        } catch (Exception e) {
            LOGGER.error("Unable to use action '{}' due to unexpected error.", metadata.getName(), e);
            actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
        }
    }

    /**
     * Get the row filter from parameters.
     *
     * @param parameters the transformation parameters
     * @param rowMetadata Row metadata to used to obtain information (valid/invalid, types...)
     * @return A {@link Predicate filter} for data set rows.
     */
    private Predicate<DataSetRow> getFilter(Map<String, String> parameters, RowMetadata rowMetadata) {
        final String filterAsString = parameters.get(ImplicitParameters.FILTER.getKey());
        final Predicate<DataSetRow> predicate = getFilterService().build(filterAsString, rowMetadata);
        if (scope == ScopeCategory.CELL || scope == ScopeCategory.LINE) {
            final Long rowId;
            final String rowIdAsString = parameters.get(ImplicitParameters.ROW_ID.getKey());
            if (StringUtils.isNotBlank(rowIdAsString)) {
                rowId = Long.parseLong(rowIdAsString);
            } else {
                rowId = null;
            }
            final Predicate<DataSetRow> rowFilter = r -> Objects.equals(r.getTdpId(), rowId);
            return predicate.and(rowFilter);
        } else {
            return predicate;
        }
    }

    private FilterService getFilterService() {
        return Providers.get(PolyglotFilterService.class);
    }
}
