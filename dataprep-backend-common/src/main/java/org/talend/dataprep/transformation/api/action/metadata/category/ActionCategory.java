package org.talend.dataprep.transformation.api.action.metadata.category;

/**
 * List all actions category.
 */
public enum ActionCategory {
    /**
     * Actions in this category will be displayed on column's header.
     */
    COLUMN_METADATA("column_metadata"), //
    COLUMNS("columns"), //
    MATH("math"), //
    STRINGS("strings"), //
    STRINGS_ADVANCED("strings advanced"), //
    SPLIT("split"), //
    DATE("dates"), //
    BOOLEAN("boolean"), //
    DATA_CLEANSING("data cleansing"), //
    FILTERED("filtered"), //
    DATA_BLENDING("data_blending");

    /** The category display name. */
    private final String displayName;

    /**
     * Create an action category with the given display name.
     * 
     * @param displayName the action display name.
     */
    ActionCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the action display name.
     */
    public String getDisplayName() {
        return displayName;
    }
}
