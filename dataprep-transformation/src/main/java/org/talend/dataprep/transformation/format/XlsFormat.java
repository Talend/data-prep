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

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;

/**
 * XLS format type.
 */
@Component("format#" + XlsFormat.XLSX)
public class XlsFormat extends ExportFormat {

    /** XLS format type name. */
    public static final String XLSX = "XLSX";

    public XlsFormat() {
        super(XLSX, "application/vnd.ms-excel", ".xlsx", true, true);
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.singletonList(
                Parameter.parameter(getLocale()) //
                        .setName("fileName") //
                        .setType(ParameterType.STRING) //
                        .setDefaultValue(StringUtils.EMPTY) //
                        .setCanBeBlank(false) //
                        .build(null) //
        );
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public boolean isCompatible(DataSetMetadata metadata) {
        return true;
    }

    @Override
    public boolean supportSampling() {
        return true;
    }
}
