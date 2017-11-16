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
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
    public static final String CSV = ParametersCSV.CSV_NAME_FORMAT;

    /** The default separator. */
    @Value("${default.text.separator:;}")
    private String defaultSeparator;

    /** The default enclosure character. */
    @Value("${default.text.enclosure:\"}")
    private String defaultTextEnclosure;

    /** The default escape character. */
    @Value("${default.text.escape:\"}")
    private String defaultEscapeChar;

    /** The default encoding. */
    @Value("${default.text.encoding:UTF-8}")
    private String defaultEncoding;

    /**
     * Default constructor.
     */
    public CSVFormat() {
        //@formatter:off
        super("CSV", "text/csv", ".csv", true, false,
                Arrays.asList(buildCsvDelimiterParameter(getLocale()),
                        Parameter.parameter().setName("fileName").setType(ParameterType.STRING).setDefaultValue(StringUtils.EMPTY).setCanBeBlank(false).build(null, getLocale()) //
                ));
        //@formatter:on
    }

    private static Parameter buildCsvDelimiterParameter(Locale locale) {
        return SelectParameter.selectParameter(locale).name("csv_fields_delimiter") //
                .item(";", "semiColon") //
                .item("\u0009", "tabulation") //
                .item(" ", "space") //
                .item(",", "comma") //
                .item("|", "pipe") //
                .defaultValue(";") //
                .canBeBlank(true) //
                .radio(true) //
                .build(null);
    }

    @Override
    public List<Parameter> getParameters() {
        return Arrays.asList(  //
                getCsvDelimiters(), //
                new Parameter(ParametersCSV.ENCLOSURE_CHAR, ParameterType.STRING, defaultTextEnclosure), //
                new Parameter(ParametersCSV.ESCAPE_CHAR, ParameterType.STRING, defaultEscapeChar),
                getEnclosureOptions(), //
                new Parameter("fileName", ParameterType.STRING, StringUtils.EMPTY, false, false), //
                buildCharsetParameter(LocaleContextHolder.getLocale()));
    }

    private SelectParameter getEnclosureOptions() {
        return SelectParameter.Builder.builder().name(ParametersCSV.ENCLOSURE_MODE) //
                .item(ParametersCSV.ENCLOSURE_TEXT_ONLY, ParametersCSV.ENCLOSURE_TEXT_ONLY_LABEL) //
                .item(ParametersCSV.ENCLOSURE_ALL_FIELDS, ParametersCSV.ENCLOSURE_TEXT_ALL_FIELDS_LABEL) //
                .defaultValue(ParametersCSV.ENCLOSURE_TEXT_ONLY) //
                .radio(true) //
                .build();
    }

    private SelectParameter getCsvDelimiters() {
        return SelectParameter.Builder.builder().name(ParametersCSV.FIELDS_DELIMITER) //
                .item(";", "semiColon") //
                .item("\u0009", "tabulation") //
                .item(" ", "space") //
                .item(",", "comma") //
                .item("|", "pipe") //
                .defaultValue(defaultSeparator) //
                .canBeBlank(true) //
                .build();
    }

    private Parameter buildCharsetParameter(Locale locale) {
        SelectParameter.Builder builder = SelectParameter.Builder.builder().name(ParametersCSV.ENCODING);
        for (Charset charsetEntry : EncodingSupport.getSupportedCharsets()) {
            builder.constant(charsetEntry.name(), charsetEntry.displayName(locale));
        }
        builder.defaultValue(Charset.isSupported(defaultEncoding) ? defaultEncoding : UTF_8.name()).canBeBlank(false);
        return builder.build();
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

    public static class ParametersCSV {

        public static final String CSV_NAME_FORMAT = "CSV";

        /** Separator argument name. */
        public static final String FIELDS_DELIMITER = "csv_fields_delimiter";

        public static final String ENCLOSURE_TEXT_ONLY = "text_only";

        public static final String ENCLOSURE_ALL_FIELDS = "all_fields";

        /** Enclosure character argument name. */
        public static final String ENCLOSURE_CHAR = "csv_enclosure_character";

        /** Enclosure character argument name. */
        public static final String ENCLOSURE_MODE = "csv_enclosure_mode";

        public static final String ENCLOSURE_TEXT_ONLY_LABEL = "custom_csv_enclosure_text_only";

        public static final String ENCLOSURE_TEXT_ALL_FIELDS_LABEL = "custom_csv_enclosure_all_fields";

        /** Escape character argument name. */
        public static final String ESCAPE_CHAR = "csv_escape_character";

        public static final String ENCODING = "csv_encoding";

        private ParametersCSV() {
        }
    }

}
