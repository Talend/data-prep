//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.format;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.format.export.ExportFormat;

import com.google.common.collect.Lists;

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
                Lists.newArrayList(
                        new Parameter("csvSeparator",
                                "CHOOSE_SEPARATOR",
                                "radio",
                                new ParameterValue(";", "SEPARATOR_SEMI_COLON"),
                                Arrays.asList(
                                        new ParameterValue("\u0009", "SEPARATOR_TAB"), // &#09;
                                        new ParameterValue(" ", "SEPARATOR_SPACE"),
                                        new ParameterValue(",", "SEPARATOR_COMMA")
                                )
                        ),
                        new Parameter(Parameter.FILENAME_PARAMETER,
                                "EXPORT_FILENAME", 
                                "text",
                                new ParameterValue(StringUtils.EMPTY, "EXPORT_FILENAME_DEFAULT"),
                                Collections.emptyList()
                        )
                ));
        //@formatter:on
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
