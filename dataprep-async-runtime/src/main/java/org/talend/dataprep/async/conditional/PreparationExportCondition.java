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

package org.talend.dataprep.async.conditional;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.export.ExportParameters;

/**
 * Return TRUE if export deal with preparation (there is a preparationId and export parameters) and not a FILTER
 */
@Component
public class PreparationExportCondition implements ConditionalTest {

    @Override
    public boolean apply(Object... args) {

        // check pre-condition
        assert args != null;
        assert args.length == 1;
        assert args[0] instanceof ExportParameters;

        return StringUtils.isNotEmpty(((ExportParameters) args[0]).getPreparationId())
                && ((ExportParameters) args[0]).getFrom() != ExportParameters.SourceType.FILTER;
    }

}
