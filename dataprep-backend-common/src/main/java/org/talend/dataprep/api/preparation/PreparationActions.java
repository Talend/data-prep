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

import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    /**
     * Transform actions list to readable JSON string
     *
     * @throws IOException
     */
    public String serializeActions() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actions);
    }

    @Override
    public String id() {
        return getId();
    }

    @Override
    public String getId() {
        try {
            return DigestUtils.sha1Hex(serializeActions());
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_COMPUTE_ID, e);
        }
    }

    @Override
    public void setId(String id) {
        // No op
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
        String serializedActions;
        try {
            serializedActions = serializeActions();
        } catch (IOException e) {
            serializedActions = "invalid actions";
        }
        return "PreparationActions {" + "id:'" + id() + "', version: " + appVersion + ", actions: " + serializedActions + '}';
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
