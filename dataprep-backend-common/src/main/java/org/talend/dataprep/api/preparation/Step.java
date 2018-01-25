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

package org.talend.dataprep.api.preparation;

import static org.talend.dataprep.api.preparation.PreparationActions.ROOT_ACTIONS;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents one step of a {@link Preparation}.
 */
public class Step extends Identifiable implements Serializable {

    public static final Step ROOT_STEP = new Step("f6e172c33bdacbc69bca9d32b2bd78174712a171");

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The parent step. */
    private String parent = "f6e172c33bdacbc69bca9d32b2bd78174712a171";

    /** The default preparation actions is the root actions. */
    private String preparationActions = ROOT_ACTIONS.id();

    /** The app version. */
    @JsonProperty("app-version")
    private String appVersion;

    /** The if there are any created column with this step. */
    private StepDiff diff;

    /** The step row metadata. */
    private String rowMetadata;

    static {
        ROOT_STEP.parent = null;
    }

    /**
     * Default empty constructor;
     */
    public Step() {
        // needed for Serialization
    }

    /**
     * Private constructor with id.
     *
     * @param id the id to set.
     */
    private Step(String id) {
        this.id = id;
    }

    /**
     * Constructor.
     *
     * @param parent the parent step.
     * @param content the step content.
     * @param appVersion the app version.
     */
    public Step(final String parent, final String content, final String appVersion) {
        this(parent, content, appVersion, null);
    }

    /**
     * Constructor.
     *
     * @param parent the parent step.
     * @param content the action content.
     * @param appVersion the app version.
     * @param diff the step diff.
     */
    public Step(final String parent, final String content, final String appVersion, final StepDiff diff) {
        this.id = UUID.randomUUID().toString();
        this.parent = parent;
        this.preparationActions = content;
        this.appVersion = appVersion;
        this.diff = diff;
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
        if (ROOT_STEP.id.equals(id)) {
            parent = null;
        }
        this.id = id;
    }

    public String getContent() {
        return preparationActions == null ? PreparationActions.ROOT_ACTIONS.id() : preparationActions;
    }

    public void setContent(String preparationActions) {
        if (Step.ROOT_STEP.id().equals(id) && !PreparationActions.ROOT_ACTIONS.id().equals(preparationActions)) {
            throw new IllegalArgumentException("Preparation action '" + preparationActions + "' is not valid for root step.");
        }
        this.preparationActions = preparationActions;
    }

    /**
     * @return The row metadata linked to this step. Might be <code>null</code> to indicate no row metadata is present.
     */
    public String getRowMetadata() {
        return rowMetadata;
    }

    /**
     * Set the row metadata for this step.
     *
     * @param rowMetadata The row metadata to set for this step.
     */
    public void setRowMetadata(String rowMetadata) {
        this.rowMetadata = rowMetadata;
    }

    @Override
    public String toString() {
        String result = "Step{parentId='";
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Step step = (Step) o;
        return Objects.equals(getId(), step.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, preparationActions, rowMetadata, appVersion, diff);
    }

}
