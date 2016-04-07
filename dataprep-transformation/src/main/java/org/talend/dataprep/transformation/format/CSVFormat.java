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

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;

/**
 * CSV format type.
 */
@Component("format#" + CSVFormat.CSV)
public class CSVFormat extends ExportFormat {

    /** CSV format type name. */
    public static final String CSV = "CSV";

    /**
     * Default constructor.
     */
    public CSVFormat() {
        //@formatter:off
        super("CSV", "text/csv", ".csv", true, false,
                Arrays.asList(SelectParameter.Builder.builder().name("CHOOSE_SEPARATOR")
                        .item(";", "SEPARATOR_PIPE")
                        .item("\u0009", "SEPARATOR_TAB")
                        .item(" ", "SEPARATOR_SPACE")
                        .item(",", "SEPARATOR_COMMA")
                        .defaultValue(";")
                        .build(),
                new Parameter("fileName",
                        ParameterType.STRING,
                        "")
        ));
        //@formatter:on
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
