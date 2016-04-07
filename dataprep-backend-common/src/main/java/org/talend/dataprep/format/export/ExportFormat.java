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

package org.talend.dataprep.format.export;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.format.export.json.ExportFormatSerializer;
import org.talend.dataprep.parameters.Parameterizable;
import org.talend.dataprep.parameters.Parameter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Models a type of format.
 */
@JsonSerialize(using = ExportFormatSerializer.class)
public abstract class ExportFormat extends Parameterizable {

    /** Prefix that must be used for all export parameters. */
    public static final String PREFIX = "exportParameters.";

    /** The format type human readable name. */
    private final String name;

    /** The mime type. */
    private final String mimeType;

    /** The file extension. */
    private final String extension;

    /** Is it the default format. */
    private final boolean defaultExport;

    /** Whether export is enabled or not (enabled by default). */
    private boolean enabled;

    /** An optional message used to explain why {@link #isEnabled()} returned false */
    private String disableReason;

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
        super(parameters, needParameters);
        this.name = name;
        this.mimeType = mimeType;
        this.extension = extension;
        this.defaultExport = defaultExport;
        this.enabled = true;
    }

    /**
     * @return A indicative order to order different {@link ExportFormat} instances.
     */
    public abstract int getOrder();

    /**
     * Although ExportFormat may be created, various external factors (OS, licencing...) may disable the export.
     * 
     * @return <code>true</code> if export format is 'enabled' (i.e. usable), <code>false</code> otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether export format is enabled or not. Intentionally left protected as only subclasses needs this at the
     * moment.
     * 
     * @param enabled <code>true</code> enable export, <code>false</code> to disable it.
     */
    protected void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * In case of {@link #isEnabled()} returning <code>false</code>, this method may return additional information to
     * indicate to UI why export was disabled.
     * 
     * @return A message that explains why export is disabled, if {@link #isEnabled()} returns empty string.
     */
    public String getDisableReason() {
        if (isEnabled()) {
            return StringUtils.EMPTY;
        }
        return disableReason;
    }

    /**
     * Sets the reason why this export was disabled.
     * 
     * @param disableReason A string to indicate why this export format was disabled, <code>null</code> is treated same
     * as empty string.
     */
    protected void setDisableReason(String disableReason) {
        if (disableReason == null) {
            this.disableReason = StringUtils.EMPTY;
        } else {
            this.disableReason = disableReason;
        }
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
     * @return true if it's the default format.
     */
    public boolean isDefaultExport() {
        return defaultExport;
    }

    /**
     * @return the Name
     */
    public String getName() {
        return name;
    }

}
