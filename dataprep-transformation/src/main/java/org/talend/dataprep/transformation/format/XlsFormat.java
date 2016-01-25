package org.talend.dataprep.transformation.format;

import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.format.export.ExportFormat;

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
                        new Parameter( Parameter.FILENAME_PARAMETER, //
                            "EXPORT_FILENAME",  //
                            "text", //
                            new ParameterValue( StringUtils.EMPTY, "EXPORT_FILENAME_DEFAULT" ), //
                            Collections.emptyList() //
                )));
    }
    //@formatter:on

    @Override
    public int getOrder() {
        return 1;
    }
}
