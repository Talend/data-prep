package org.talend.dataprep.dataset.service.api;

import java.util.List;

import org.talend.dataprep.i18n.DataprepBundle;
import org.talend.dataprep.parameters.Parameter;

public class Import {

    private final String locationType;

    private final String contentType;

    private final List<Parameter> parameters;

    private final boolean dynamic;

    private final boolean defaultImport;

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
        return DataprepBundle.message("import." + locationType + ".label");
    }

    public String getTitle() {
        return DataprepBundle.message("import." + locationType + ".title");
    }
}
