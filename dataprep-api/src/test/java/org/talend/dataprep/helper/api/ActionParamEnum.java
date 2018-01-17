package org.talend.dataprep.helper.api;

import java.util.Arrays;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * {@link Enum} representation of {@link Action} parameters types.
 */
public enum ActionParamEnum {
    FROM_PATTERN_MODE("fromPatternMode", "from_pattern_mode"),
    NEW_PATTERN("newPattern", "new_pattern"),
    SCOPE("scope", "scope"),
    COLUMN_NAME("columnName", "column_name"),
    COLUMN_ID("columnId", "column_id"),
    ROW_ID("rowId", "row_id"),
    LIMIT("limit", "limit"),
    SEPARATOR("separator", "separator"),
    MANUAL_SEPARATOR_STRING("manualSeparatorString", "manual_separator_string"),
    MANUAL_SEPARATOR_REGEX("manualSeparatorRegex", "manual_separator_regex"),
    FILTER("filter", "filter"),
    REGION_CODE("regionCode","region_code"),
    MODE("mode","mode"),
    FORMAT_TYPE("formatType","format_type");

    private String name;

    private String jsonName;

    ActionParamEnum(String pName, String pJsonName) {
        name = pName;
        jsonName = pJsonName;
    }

    /**
     * Get a corresponding {@link ActionParamEnum} from a {@link String}.
     *
     * @param pName the {@link ActionParamEnum#name}.
     * @return the corresponding {@link ActionParamEnum} or <code>null</code> if there isn't.
     */
    @Nullable
    public static ActionParamEnum getActionParamEnum(@NotNull String pName) {
        return Arrays.stream(ActionParamEnum.values()) //
                .filter(e -> e.name.equalsIgnoreCase(pName)) //
                .findFirst() //
                .orElse(null);
    }

    public String getName() {
        return name;
    }

    @JsonValue
    public String getJsonName() {
        return jsonName;
    }
}
