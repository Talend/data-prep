package org.talend.dataprep.api.dataset;

import static java.util.Collections.emptyList;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.i18n.DataprepBundle;
import org.talend.dataprep.parameters.Parameter;

/**
 * Bean that is used to display the supported import types for dataprep.
 */
public class Import {

    private String locationType;

    private String contentType;

    /** If the import has some parameters to display by the front end. */
    private List<Parameter> parameters;

    /** If the import is dynamic, the parameters are provided by the backend via another rest call. */
    private boolean dynamic;

    /** True if it's the default import. */
    private boolean defaultImport;

    /** The import label. */
    private String label = null;

    /** The import form title. */
    private String title = null;

    /**
     * Default constructor for jackson deserialization
     */
    public Import() {
    }

    /**
     * Constructor.
     *
     * @param locationType the location type.
     * @param contentType the content type.
     * @param parameters the import parameters.
     * @param dynamic if the import parameters are dynamic and need to sent from the backend.
     * @param defaultImport if this import is the default one.
     */
    public Import(String locationType, String contentType, List<Parameter> parameters, boolean dynamic, boolean defaultImport) {
        this.locationType = locationType;
        this.contentType = contentType;
        this.parameters = parameters;
        this.dynamic = dynamic;
        this.defaultImport = defaultImport;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean isDefaultImport() {
        return defaultImport;
    }

    public String getContentType() {
        return contentType;
    }

    public String getLocationType() {
        return locationType;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public String getLabel() {
        if (StringUtils.isBlank(this.label)) {
            return DataprepBundle.message("import." + locationType + ".label");
        } else {
            return this.label;
        }
    }

    public String getTitle() {
        if (StringUtils.isBlank(this.title)) {
            return DataprepBundle.message("import." + locationType + ".title");
        } else {
            return this.title;
        }
    }

    private void setLabel(String label) {
        this.label = label;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public void setDefaultImport(boolean defaultImport) {
        this.defaultImport = defaultImport;
    }

    /**
     * Import builder to ease the use of the huge constructor.
     */
    public static class ImportBuilder {

        private String locationType;

        private String contentType;

        private List<Parameter> parameters = emptyList();

        private boolean dynamic;

        private boolean defaultImport;

        private String label;

        private String title;

        private ImportBuilder() {
            // nothing to do here
        }

        public static ImportBuilder builder() {
            return new ImportBuilder();
        }

        public ImportBuilder locationType(String locationType) {
            this.locationType = locationType;
            return this;
        }

        public ImportBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public ImportBuilder label(String label) {
            this.label = label;
            return this;
        }

        public ImportBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ImportBuilder parameters(List<Parameter> parameters) {
            if (parameters != null) {
                this.parameters = parameters;
            }
            return this;
        }

        public ImportBuilder dynamic(boolean dynamic) {
            this.dynamic = dynamic;
            return this;
        }

        public ImportBuilder defaultImport(boolean defaultImport) {
            this.defaultImport = defaultImport;
            return this;
        }

        public Import build() {
            Import result = new Import(locationType, contentType, parameters, dynamic, defaultImport);
            if (StringUtils.isNotBlank(this.label)) {
                result.setLabel(this.label);
            }
            if (StringUtils.isNotBlank(this.title)) {
                result.setTitle(this.title);
            }
            return result;
        }
    }
}
