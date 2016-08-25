//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.preparation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.apache.commons.codec.digest.DigestUtils;

import static java.util.Collections.unmodifiableList;

/**
 * Models a list of actions for a step within a preparation.
 */
public class PreparationActions extends Identifiable implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The list of actions. */
    private final List<Action> actions;

    /** The app version. */
    @JsonProperty("app-version")
    private String appVersion;

    /**
     * Default empty constructor.
     */
    public PreparationActions() {
        // needed for mongodb integration
        this.actions = Collections.emptyList();
    }

    /**
     * Default constructor.
     *
     * @param appVersion the current application version for this PreparationActions.
     */
    public PreparationActions(String appVersion) {
        this(Collections.emptyList(), appVersion);
    }

    /**
     * Create the PreparationActions with the given actions.
     *
     * @param actions the actions for this preparation.
     * @param appVersion the current application version for this PreparationActions.
     */
    @JsonCreator
    public PreparationActions(@JsonProperty("actions") final List<Action> actions, //
                              @JsonProperty("app-version") String appVersion) {
        this.actions = unmodifiableList(actions);
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

    /**
     * @return the AppVersion
     */
    public String getAppVersion() {
        return appVersion;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("actions", actions).add("appVersion", appVersion).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PreparationActions that = (PreparationActions) o;
        return Objects.equals(actions, that.actions) && Objects.equals(appVersion, that.appVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actions, appVersion);
    }
}
