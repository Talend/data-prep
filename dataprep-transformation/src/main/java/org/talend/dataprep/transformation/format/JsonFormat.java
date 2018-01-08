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

package org.talend.dataprep.transformation.format;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.parameters.Parameter;

/**
 * Json format type.
 */
@Component("format#" + JsonFormat.JSON)
public class JsonFormat extends ExportFormat {

    /** Json format type name. */
    public static final String JSON = "JSON";

    /**
     * Default constructor.
     */
    public JsonFormat() {
        super(JSON, "application/json", ".json", false, false);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public boolean isCompatible(DataSetMetadata metadata) {
        return true;
    }

    @Override
    public boolean supportSampling() {
        return true;
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }
}
