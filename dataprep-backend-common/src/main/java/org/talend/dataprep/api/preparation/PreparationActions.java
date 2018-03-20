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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Models a list of actions for a step within a preparation.
 */
public class PreparationActions extends Identifiable implements Serializable {

    public static final PreparationActions ROOT_ACTIONS = new PreparationActions("cdcd5c9a3a475f2298b5ee3f4258f8207ba10879");

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The list of actions. */
    private List<Action> actions = Collections.emptyList();

    /** The app version. */
    @JsonProperty("app-version")
    private String appVersion;

    /**
     * Private constructor with the ID for the ROOT_ACTION.
     *
     * @param id the preparation actions id.
     */
    private PreparationActions(String id) {
        this.id = id;
    }

    /**
     * Default empty constructor.
     */
    public PreparationActions() {
        // needed for serialization
    }

    /**
     * Create the PreparationActions with the given actions.
     *
     * @param actions the actions for this preparation.
     * @param appVersion the current application version for this PreparationActions.
     */
    public PreparationActions(final List<Action> actions, String appVersion) {
        this.actions = Collections.unmodifiableList(actions);
        this.appVersion = appVersion;
    }

    /**
     * Create a new PreparationActions with concatenated new Actions
     *
     * @param newActions - the actions to add
     * @return - the new preparation actions
     */
    public PreparationActions append(final List<Action> newActions) {
        final List<Action> appendedActions = new ArrayList<>(getActions().size() + newActions.size());
        appendedActions.addAll(actions);
        appendedActions.addAll(newActions);
        return new PreparationActions(appendedActions, getAppVersion());
    }

    @Override
    public String id() {
        return getId();
    }

    // JSon serialization is not deterministic due to actions hashmap order. We can't use it to create a stable id.
    @Override
    public String getId() {
        if (id == null) {
            id = DigestUtils.sha1Hex(Integer.toString(hashCode()));
        }
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Return the immutable list of actions
     *
     * @return - the list of actions
     */
    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    /**
     * @return the AppVersion
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * @param appVersion the appVersion to set.
     */
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public String toString() {
        return "PreparationActions{" + //
                "id='" + id + '\'' + //
                ", actions=" + actions + //
                ", appVersion='" + appVersion + '\'' + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PreparationActions that = (PreparationActions) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        // ID must not be part of that because the hashcode is used for the ID !
        return Objects.hash(actions, appVersion);
    }
}
