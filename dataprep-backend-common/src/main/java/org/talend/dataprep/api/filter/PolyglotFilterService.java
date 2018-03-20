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

package org.talend.dataprep.api.filter;

import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

/**
 * A {@link FilterService} implementation that understands both JSON and TQL as filter.
 */
public class PolyglotFilterService implements FilterService {

    private final SimpleFilterService jsonFilterService = new SimpleFilterService();

    private final TQLFilterService tqlFilterService = new TQLFilterService();

    @Override
    public Predicate<DataSetRow> build(String filterAsString, RowMetadata rowMetadata) {
        if (StringUtils.isBlank(filterAsString)) {
            return row -> true;
        }
        try {
            return selectFilterService(filterAsString).build(filterAsString, rowMetadata);
        } catch (Exception e) {
            throw new TalendRuntimeException(BaseErrorCodes.UNABLE_TO_PARSE_FILTER, e);
        }
    }

    private FilterService selectFilterService(String filterAsString) {
        if (filterAsString.startsWith("{")) {
            return jsonFilterService;
        } else {
            return tqlFilterService;
        }
    }
}
