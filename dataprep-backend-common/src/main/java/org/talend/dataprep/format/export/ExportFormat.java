package org.talend.dataprep.format.export;

import java.io.Serializable;
import java.util.List;

import org.talend.dataprep.format.export.json.ExportFormatSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Models a type of format.
 */
@JsonSerialize(using = ExportFormatSerializer.class)
public class ExportFormat {

    /** Prefix that must be used for all export parameters. */
    public static final String PREFIX = "exportParameters.";

    /** The format type human readable name. */
    private final String name;
    /** The mime type. */
    private final String mimeType;
    /** The file extension. */
    private final String extension;

    /** Does this format type need more parameters? (ui will open a new form in this case). */
    private final boolean needParameters;

    /** Is it the default format. */
    private final boolean defaultExport;

    /** List of extra parameters needed for this format (i.e separator for csv files etc...). */
    private final List<Parameter> parameters;

    /**
     * Default protected constructor.
     *
     * @param name the format type human readable name.
     * @param mimeType the format mime type.
     * @param extension the file extension.
     * @param needParameters if the type needs parameters.
     * @param defaultExport if it's the default format.
     * @param parameters the list of parameters.
     */
    public ExportFormat(final String name, final String mimeType, final String extension, final boolean needParameters,
            final boolean defaultExport, final List<Parameter> parameters) {
        this.name = name;
        this.mimeType = mimeType;
        this.extension = extension;
        this.needParameters = needParameters;
        this.defaultExport = defaultExport;
        this.parameters = parameters;
    }

    /**
     * @return the mime type.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return the file extension.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @return true if parameters are needed.
     */
    public boolean isNeedParameters() {
        return needParameters;
    }

    /**
     * @return true if it's the default format.
     */
    public boolean isDefaultExport() {
        return defaultExport;
    }

    /**
     * @return the list of needed parameters.
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * @return the Name
     */
    public String getName() {
        return name;
    }

    /**
     * Inner Parameter class.
     */
    public static class Parameter implements Serializable {

        /** Serialization UID. */
        private static final long serialVersionUID = 1L;

        /** Common filename parameter name. */
        public static final String FILENAME_PARAMETER = "fileName";

        /** Parameter name. */
        private final String name;

        /** Can be used as a label key in the ui. */
        private final String labelKey;

        /** The default value for the parameter must not be in values. */
        private final ParameterValue defaultValue;

        /** All possible values for the parameter. */
        private final List<ParameterValue> values;

        /** Html type (input type: radio, text). */
        private final String type;

        /**
         * Constructor.
         *
         * param name the parameter name.
         * 
         * @param labelKey the label key that may be used by the ui.
         * @param type the parameter html type.
         * @param defaultValue the parameter default value.
         * @param values the parameters values.
         */
        public Parameter(String name, String labelKey, String type, ParameterValue defaultValue, List<ParameterValue> values) {
            this.name = name;
            this.labelKey = labelKey;
            this.defaultValue = defaultValue;
            this.values = values;
            this.type = type;
        }

        /**
         * @return the parameter name.
         */
        public String getName() {
            return name;
        }

        /**
         * @return the label to use by the UI.
         */
        public String getLabelKey() {
            return labelKey;
        }

        /**
         * @return the default value.
         */
        public ParameterValue getDefaultValue() {
            return defaultValue;
        }

        /**
         * @return the list of parameters value.
         */
        public List<ParameterValue> getValues() {
            return values;
        }

        /**
         * @return the type.
         */
        public String getType() {
            return type;
        }
    }

    /**
     * Inner class for parameter value.
     */
    public static class ParameterValue {

        /** The parameter value. */
        private final String value;

        /** The label to use by the UI. */
        private final String labelKey;

        /**
         * Constructor.
         *
         * @param value the parameter value.
         * @param labelKey the label to use by the UI.
         */
        public ParameterValue(String value, String labelKey) {
            this.value = value;
            this.labelKey = labelKey;
        }

        /**
         * @return the parameter value.
         */
        public String getValue() {
            return value;
        }

        /**
         * @return the label to use by the UI.
         */
        public String getLabelKey() {
            return labelKey;
        }
    }

}
