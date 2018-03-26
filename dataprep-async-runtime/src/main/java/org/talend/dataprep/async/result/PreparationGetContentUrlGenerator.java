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

package org.talend.dataprep.async.result;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.async.AsyncExecutionResult;

@Component
public class PreparationGetContentUrlGenerator implements ResultUrlGenerator {

    @Override
    public AsyncExecutionResult generateResultUrl(Object... args) {

        // check pre-condition
        Validate.notNull(args);
        Validate.isTrue(args.length == 1);
        Validate.isInstanceOf(ExportParameters.class, args[0]);

        ExportParameters param = (ExportParameters) args[0];

        URIBuilder builder = new URIBuilder();
        builder.setPath("/api/preparations/" + param.getPreparationId() + "/content");

        if (StringUtils.isNotEmpty(param.getStepId())) {
            builder.setParameter("version", param.getStepId());
        }

        if (param.getFrom() != null) {
            builder.setParameter("from", param.getFrom().name());
        }

        if(StringUtils.isNotEmpty(param.getFilter())) {
            builder.setParameter("filter", param.getFilter());
        }

        return new AsyncExecutionResult(builder.toString());
    }
}
