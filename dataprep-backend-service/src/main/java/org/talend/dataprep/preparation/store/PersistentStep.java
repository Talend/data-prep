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

package org.talend.dataprep.preparation.store;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.StepDiff;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A {@link org.talend.dataprep.api.preparation.Step} for persistent storage.
 *
 * @see org.talend.dataprep.conversions.BeanConversionService
 * @see org.talend.dataprep.configuration.PreparationRepositoryConfiguration.PersistentPreparationConversions
 */
public class PersistentStep extends PersistentIdentifiable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The parent step. */
    private String parent;

    /** The default preparation actions is the root actions. */
    private String preparationActions;

    /** The app version. */
    @JsonProperty("app-version")
    private String appVersion;

    /** The if there are any created column with this step. */
    private StepDiff diff;

    /** The step row metadata. */
    private RowMetadata rowMetadata;

    /**
     * Default empty constructor;
     */
    public PersistentStep() {
        // needed for Serialization
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public StepDiff getDiff() {
        return diff;
    }

    public void setDiff(StepDiff diff) {
        this.diff = diff;
    }

    public String getAppVersion() {
        return appVersion;
    }

    @Override
    public String id() {
        return getId();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return preparationActions;
    }

    public void setContent(String preparationActions) {
        this.preparationActions = preparationActions;
    }

    /**
     * @return The row metadata linked to this step. Might be <code>null</code> to indicate no row metadata is present.
     */
    public RowMetadata getRowMetadata() {
        return rowMetadata;
    }

    /**
     * Set the row metadata for this step.
     *
     * @param rowMetadata The row metadata to set for this step.
     */
    public void setRowMetadata(RowMetadata rowMetadata) {
        this.rowMetadata = rowMetadata;
    }

    @Override
    public String toString() {
        String result = "PersistentStep{parentId='";
        if (parent != null) {
            result += parent;
        } else {
            result += "null";
        }
        result += '\'' + //
                ", actions='" + preparationActions + '\'' + //
                ", appVersion='" + appVersion + '\'' + //
                ", diff=" + diff + //
                '}';
        return result;
    }

}
