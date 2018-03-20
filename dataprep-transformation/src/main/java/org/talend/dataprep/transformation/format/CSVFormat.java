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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
        super(ParametersCSV.CSV_NAME_FORMAT, "text/csv", ".csv", true, false);
    }

    @Override
    public List<Parameter> getParameters() {
        Locale currentLocale = getLocale();
        return Arrays.asList(//
                getFileName(currentLocale), //
                getCsvDelimiters(currentLocale), //
                getEnclosureChar(currentLocale), //
                getEscapeChar(currentLocale), //
                getEnclosureOptions(currentLocale), //
                buildCharsetParameter(currentLocale));
    }

    private Parameter getFileName(Locale locale) {
        return Parameter.parameter(locale) //
                .setName("fileName") //
                .setType(ParameterType.STRING) //
                .setDefaultValue(StringUtils.EMPTY) //
                .setImplicit(false) //
                .setCanBeBlank(false) //
                .build(null);
    }

    private Parameter getEnclosureChar(Locale locale) {
        return Parameter.parameter(locale) //
                .setName(ParametersCSV.ENCLOSURE_CHAR) //
                .setType(ParameterType.STRING) //
                .setDefaultValue(defaultTextEnclosure) //
                .build(null);
    }

    private Parameter getEscapeChar(Locale locale) {
        return Parameter.parameter(locale) //
                .setName(ParametersCSV.ESCAPE_CHAR) //
                .setType(ParameterType.STRING) //
                .setDefaultValue(defaultEscapeChar) //
                .build(null);
    }

    private SelectParameter getEnclosureOptions(Locale locale) {
        return SelectParameter.selectParameter(locale) //
                .name(ParametersCSV.ENCLOSURE_MODE) //
                .item(ParametersCSV.ENCLOSURE_ALL_FIELDS, ParametersCSV.ENCLOSURE_TEXT_ALL_FIELDS_LABEL) //
                .item(ParametersCSV.ENCLOSURE_TEXT_ONLY, ParametersCSV.ENCLOSURE_TEXT_ONLY_LABEL) //
                .defaultValue(ParametersCSV.ENCLOSURE_TEXT_ONLY) //
                .radio(true) //
                .build(null);
    }

    private SelectParameter getCsvDelimiters(Locale locale) {
        return SelectParameter.selectParameter(locale) //
                .name(ParametersCSV.FIELDS_DELIMITER) //
                .item(";", "semiColon") //
                .item("\u0009", "tabulation") //
                .item(" ", "space") //
                .item(",", "comma") //
                .item("|", "pipe") //
                .defaultValue(defaultSeparator) //
                .canBeBlank(true) //
                .build(null);
    }

    private Parameter buildCharsetParameter(Locale locale) {
        SelectParameter.SelectParameterBuilder builder = SelectParameter.selectParameter(locale).name(ParametersCSV.ENCODING);
        for (Charset charsetEntry : EncodingSupport.getSupportedCharsets()) {
            builder.constant(charsetEntry.name(), charsetEntry.displayName(locale));
        }
        builder.defaultValue(Charset.isSupported(defaultEncoding) ? defaultEncoding : UTF_8.name()).canBeBlank(false);
        return builder.build(null);
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
