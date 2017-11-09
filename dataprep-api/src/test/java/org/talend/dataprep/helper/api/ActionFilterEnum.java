package org.talend.dataprep.helper.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * {@link Enum} representation of the filter part af an {@link Action}.
 */
public enum ActionFilterEnum {
    FIELD("filter.field", "field"), //
    START("filter.start", "start"), //
    END("filter.end", "end"), //
    TYPE("filter.type", "type"), //
    LABEL("filter.label", "label");

    private String name;

    private String jsonName;

    ActionFilterEnum(String pName, String pJsonName) {
        name = pName;
        jsonName = pJsonName;
    }

    public static ActionFilterEnum getActionFilterEnum(String pName) {
        List<ActionFilterEnum> ret = Arrays.stream(ActionFilterEnum.values()) //
                .filter(e -> e.name.equals(pName)) //
                .collect(Collectors.toList());
        return ret.size() == 0 ? null : ret.get(0);
    }

    public String getName() {
        return name;
    }

    @JsonValue
    public String getJsonName() {
        return jsonName;
    }
}
