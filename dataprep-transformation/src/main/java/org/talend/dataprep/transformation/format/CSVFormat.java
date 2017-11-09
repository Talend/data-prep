// ============================================================================
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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.configuration.EncodingSupport;
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

    private static final SelectParameter CSV_DELIMITERS = SelectParameter.Builder.builder().name(Parameters.FIELDS_DELIMITER) //
            .item(";", "semiColon") //
            .item("\u0009", "tabulation") //
            .item(" ", "space") //
            .item(",", "comma") //
            .item("|", "pipe") //
            .defaultValue(";") //
            .canBeBlank(true) //
            .build();

    private static final SelectParameter ENCLOSURE_OPTIONS = SelectParameter.Builder.builder().name(Parameters.ENCLOSURE_MODE) //
            .item(Parameters.ENCLOSURE_TEXT_ONLY, "custom_csv_enclosure_text_only") //
            .item(Parameters.ENCLOSURE_ALL_FIELDS, "custom_csv_enclosure_all_fields") //
            .defaultValue(Parameters.ENCLOSURE_TEXT_ONLY) //
            .radio(true) //
            .build();

    /**
     * Default constructor.
     */
    public CSVFormat() {
        //@formatter:off
        super("CSV", "text/csv", ".csv", true, false,
                Arrays.asList( //
                        CSV_DELIMITERS, //
                        new Parameter(Parameters.ESCAPE_CHAR, ParameterType.STRING, StringUtils.EMPTY),
                        new Parameter(Parameters.ENCLOSURE_CHAR, ParameterType.STRING, StringUtils.EMPTY), //
                        ENCLOSURE_OPTIONS, //
                        new Parameter("fileName", ParameterType.STRING, StringUtils.EMPTY, false, false), //
                        buildCharsetParameter(LocaleContextHolder.getLocale()) //
        ));
        //@formatter:on
    }

    private static Parameter buildCharsetParameter(Locale locale) {
        SelectParameter.Builder builder = SelectParameter.Builder.builder().name(Parameters.ENCODING);
        for (Charset charsetEntry : EncodingSupport.getSupportedCharsets()) {
            builder.constant(charsetEntry.name(), charsetEntry.displayName(locale));
        }
        return builder.defaultValue(UTF_8.name()).canBeBlank(false).build();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isCompatible(DataSetMetadata metadata) {
        return true;
    }

    @Override
    public boolean supportSampling() {
        return true;
    }

    public static class Parameters {

        /** Separator argument name. */
        public static final String FIELDS_DELIMITER = "csv_fields_delimiter";

        public static final String ENCLOSURE_TEXT_ONLY = "text_only";

        public static final String ENCLOSURE_ALL_FIELDS = "all_fields";

        /** Escape character argument name. */
        public static final String ESCAPE_CHAR = "csv_escape_character";

        /** Enclosure character argument name. */
        public static final String ENCLOSURE_CHAR = "csv_enclosure_character";

        /** Enclosure character argument name. */
        public static final String ENCLOSURE_MODE = "csv_enclosure_mode";

        public static final String ENCODING = "csv_encoding";

        private Parameters() {
        }
    }

}
