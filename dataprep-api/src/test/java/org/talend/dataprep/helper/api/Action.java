// ============================================================================
//
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

package org.talend.dataprep.helper.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Action In/Out representation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Action {

    private static Map<String, List<String>> ignoredParams = new HashMap<>();
    static {
        ignoredParams.put("lookup", Arrays.asList("lookup_ds_id"));
        ignoredParams.put("split", Arrays.asList("row_id"));
        ignoredParams.put("extract_date_tokens", Arrays.asList("row_id"));
        ignoredParams.put("delete_invalid", Arrays.asList("row_id"));
        ignoredParams.put("change_date_pattern", Arrays.asList("row_id"));
        ignoredParams.put("change_number_format", Arrays.asList("row_id"));
        ignoredParams.put("uppercase", Arrays.asList("row_id"));
    }

    public String action;

    // not to be loaded by jackson but to be inferred from steps attribute @see PreparationDetails
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String id;

    public Map<String, Object> parameters = new HashMap<>();

    /**
     * Verify if we should ignore a parameter during equals check.
     *
     * @param actionName the action name
     * @param paramName the parameter name
     * @return <code>true</code> or <code>false</code>
     */
    private static boolean isIgnored(String actionName, String paramName) {
        List<String> ignoredParam = ignoredParams.get(actionName);
        if (ignoredParam != null) {
            if (ignoredParam.contains(paramName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Specific equals in order to ignore lookup_ds_id parameter from lookup actions.
     *
     * @param o action to check equality with.
     * @return <code>true</code> if both objects are equals, <code>else</code> else.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Action))
            return false;

        Action action1 = (Action) o;

        if (action != null ? !action.equals(action1.action) : action1.action != null)
            return false;

        if (parameters.size() != action1.parameters.size())
            return false;

        Iterator<Map.Entry<String, Object>> esIterator = parameters.entrySet().iterator();
        while (esIterator.hasNext()) {
            Map.Entry<String, Object> entry = esIterator.next();
            String key = entry.getKey();
            if (!isIgnored(action, key)) {
                Object value = entry.getValue();
                if (value == null) {
                    if (!(action1.parameters.get(key) == null && action1.parameters.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(action1.parameters.get(key)))
                        return false;
                }
            }
        }
        return true;
    }

    // Generated hashCode() on action & parameters attributes
    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }
}
