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

import java.util.List;

import org.talend.dataprep.api.action.ActionForm;

/**
 * Bean that wraps Preparation used for json serialization towards frontend.
 */
public class PreparationMessage extends Preparation {

    /** Allow run of preparation on distributed environment */
    private boolean allowDistributedRun;

    /** Allow full run for this preparation */
    private boolean allowFullRun;

    /** List of action metadata (description) */
    private List<ActionForm> metadata;

    /** List of actions with parameters */
    private List<Action> actions;

    /** Diff between step (e.g. to know created/deleted columns) */
    private List<StepDiff> diff;

    /**
     * @return <code>true</code> if full run is allowed for this preparation.
     */
    public boolean isAllowFullRun() {
        return allowFullRun;
    }

    /**
     * Set the allowFullRun flag for this preparation.
     *
     * @param allowFullRun <code>true</code> to allow full run, <code>false</code> otherwise.
     */
    public void setAllowFullRun(boolean allowFullRun) {
        this.allowFullRun = allowFullRun;
    }

    /**
     * @return <code>true</code> if preparation can be run in distributed environment, <code>false</code> otherwise.
     */
    public boolean isAllowDistributedRun() {
        return allowDistributedRun;
    }

    /**
     * Set the allowDistributedRun flag for this preparation.
     *
     * @param allowDistributedRun <code>true</code> to allow run in distributed environment, <code>false</code> otherwise.
     */
    public void setAllowDistributedRun(boolean allowDistributedRun) {
        this.allowDistributedRun = allowDistributedRun;
    }

    /**
     * @return The list of actions (with parameters) for this preparation.
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * The list of actions (with parameters) for this preparation.
     *
     * @param actions The list of actions in this preparation.
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    /**
     * @return The diffs between steps of this preparation.
     */
    public List<StepDiff> getDiff() {
        return diff;
    }

    /**
     * Set the diffs for this preparation.
     *
     * @param diff The diffs between steps.
     */
    public void setDiff(List<StepDiff> diff) {
        this.diff = diff;
    }

    /**
     * @return The list of action metadata in the preparation.
     */
    public List<ActionForm> getMetadata() {
        return metadata;
    }

    /**
     * Set the action descriptions in this preparation.
     *
     * @param metadata The new action descriptions for this preparation.
     */
    public void setMetadata(List<ActionForm> metadata) {
        this.metadata = metadata;
    }
}
