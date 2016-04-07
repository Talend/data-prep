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

package org.talend.dataprep.transformation.format;

import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
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

    /**
     * Default constructor.
     */
    //@formatter:off
    public XlsFormat() {
        super(XLSX, "application/vnd.ms-excel", ".xlsx", true, true,
                Collections.singletonList(
                        new Parameter( "fileName", //
                                ParameterType.STRING,  //
                                StringUtils.EMPTY,
                                false,
                                false)));
    }
    //@formatter:on

    @Override
    public int getOrder() {
        return 1;
    }
}
