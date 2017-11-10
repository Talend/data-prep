package org.talend.dataprep.helper.api;

import static org.talend.dataprep.helper.api.ParamType.INTEGER;
import static org.talend.dataprep.helper.api.ParamType.STRING;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * {@link Enum} representation of the filter part af an {@link Action}.
 */
public enum ActionFilterEnum {
    FIELD("filter.field", "field", STRING), //
    START("filter.start", "start", INTEGER), //
    END("filter.end", "end", INTEGER), //
    TYPE("filter.type", "type", STRING), //
    LABEL("filter.label", "label", STRING);

    private String name;

    private String jsonName;

    private ParamType paramType;

    ActionFilterEnum(String pName, String pJsonName, ParamType pParamType) {
        name = pName;
        jsonName = pJsonName;
        paramType = pParamType;
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

    /**
     * Return a Json compatible object depending on {@link ActionFilterEnum#paramType}
     * 
     * @param value the value to process.
     * @return an Object corresponding to the value.
     */
    public Object processValue(String value) {
        Object ret = null;
        switch (paramType) {
        case INTEGER:
            ret = new Integer(value);
            break;
        case STRING:
        default:
            ret = value;
        }
        return ret;
    }
}
