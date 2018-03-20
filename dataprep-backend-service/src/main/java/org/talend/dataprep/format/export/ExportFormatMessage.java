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

package org.talend.dataprep.format.export;

import java.util.List;

import org.talend.dataprep.parameters.Parameter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Models a type of format for DTO
 */
@SuppressWarnings("common-java:InsufficientBranchCoverage ")
public class ExportFormatMessage {

    private String mimeType;

    private String extension;

    private String id;

    private boolean needParameters;

    private boolean defaultExport;

    private boolean enabled;

    private String disableReason;

    private String title;

    // in order to be able to serialize sub-classes of Parameter
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
    private List<Parameter> parameters;

    private boolean supportSampling;

    private String name;

    public ExportFormatMessage() {
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isNeedParameters() {
        return needParameters;
    }

    public void setNeedParameters(boolean needParameters) {
        this.needParameters = needParameters;
    }

    public boolean isDefaultExport() {
        return defaultExport;
    }

    public void setDefaultExport(boolean defaultExport) {
        this.defaultExport = defaultExport;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDisableReason() {
        return disableReason;
    }

    public void setDisableReason(String disableReason) {
        this.disableReason = disableReason;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void setSupportSampling(boolean supportSampling) {
        this.supportSampling = supportSampling;
    }

    public boolean isSupportSampling() {
        return supportSampling;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ExportFormatMessage that = (ExportFormatMessage) o;

        if (needParameters != that.needParameters)
            return false;
        if (defaultExport != that.defaultExport)
            return false;
        if (enabled != that.enabled)
            return false;
        if (supportSampling != that.supportSampling)
            return false;
        if (!mimeType.equals(that.mimeType))
            return false;
        if (!extension.equals(that.extension))
            return false;
        if (!id.equals(that.id))
            return false;
        if (!disableReason.equals(that.disableReason))
            return false;
        if (!title.equals(that.title))
            return false;
        if (!parameters.equals(that.parameters))
            return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = mimeType.hashCode();
        result = 31 * result + extension.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + (needParameters ? 1 : 0);
        result = 31 * result + (defaultExport ? 1 : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + disableReason.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + parameters.hashCode();
        result = 31 * result + (supportSampling ? 1 : 0);
        result = 31 * result + name.hashCode();
        return result;
    }
}
