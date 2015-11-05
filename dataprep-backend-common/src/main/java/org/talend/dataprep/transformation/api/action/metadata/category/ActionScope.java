package org.talend.dataprep.transformation.api.action.metadata.category;

/**
 * Action scope is a concept that allow us to describe on which scope(s) each action can be applied.
 *
 * As example, and first implementation, scopes INVALID and EMPTY allow frontend to "plug" some actions on DQ bar
 * dynamically.
 *
 * More applications may be added later.
 *
 */
public enum ActionScope {

    INVALID, //
    EMPTY;

    public String getDisplayName() {
        return name().toLowerCase();
    }

}
