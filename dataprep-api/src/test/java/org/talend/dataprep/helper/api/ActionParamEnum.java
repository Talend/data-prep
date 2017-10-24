package org.talend.dataprep.helper.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    ROW_ID("rowId", "row_id");

    private String name;

    private String jsonName;

    ActionParamEnum(String pName, String pJsonName) {
        name = pName;
        jsonName = pJsonName;
    }

    public static ActionParamEnum getActionParamEnum(String pName) {
        List<ActionParamEnum> ret = Arrays.stream(ActionParamEnum.values()) //
                .filter(e -> e.name.equals(pName)) //
                .collect(Collectors.toList());
        return ret.get(0);
    }

    public String getName() {
        return name;
    }

    @JsonValue
    public String getJsonName() {
        return jsonName;
    }
}
