package org.talend.dataprep.helper.api;

import static org.talend.dataprep.helper.api.ParamType.INTEGER;
import static org.talend.dataprep.helper.api.ParamType.STRING;

import java.util.Arrays;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * {@link Enum} representation of the Filter part af an {@link Action}.
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

    /**
     * Get a corresponding {@link ActionFilterEnum} from a {@link String}.
     * 
     * @param pName the {@link ActionFilterEnum#name}.
     * @return the corresponding {@link ActionFilterEnum} or <code>null</code> if there isn't.
     */
    @Nullable
    public static ActionFilterEnum getActionFilterEnum(@NotNull String pName) {
        return Arrays
                .stream(ActionFilterEnum.values()) //
                .filter(e -> e.name.equalsIgnoreCase(pName)) //
                .findFirst()
                .orElse(null);
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
        Object ret;
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
