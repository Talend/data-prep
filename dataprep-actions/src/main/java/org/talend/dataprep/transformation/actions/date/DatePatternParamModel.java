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

package org.talend.dataprep.transformation.actions.date;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.parameters.Item;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * This interface is designed to be implemented by actions that have a date pattern as parameter.
 */
public interface DatePatternParamModel {

    /**
     * Name of the new date pattern parameter.
     */
    String NEW_PATTERN = "new_pattern"; //$NON-NLS-1$

    /**
     * The parameter object for the custom new pattern.
     */
    String CUSTOM_PATTERN = "custom_date_pattern"; //$NON-NLS-1$

    /**
     * Key to store compiled pattern in action context.
     */
    String COMPILED_DATE_PATTERN = "compiled_datePattern";

    /**
     * The parameter object for the custom new pattern.
     */
    Parameter CUSTOM_PATTERN_PARAMETER = new Parameter(CUSTOM_PATTERN, ParameterType.STRING, EMPTY, false, false);

    /**
     * @return the Parameters to display for the date related action.
     */
    default List<Parameter> getParametersForDatePattern() {

        ResourceBundle patterns = ResourceBundle
                .getBundle("org.talend.dataprep.transformation.actions.date.date_patterns", Locale.ENGLISH);
        Enumeration<String> keys = patterns.getKeys();

        List<Item> items = new ArrayList<>();
        Item defaultItem = null;
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = patterns.getString(key);
            Item item = Item.Builder.builder()
                    .value(value)
                    //.label(ActionsBundle.INSTANCE.choice(Locale.ENGLISH, "choice." + key, key + " (" + value + ")"))
                    .build();
            items.add(item);

            if ("ISO".equals(key)){
                defaultItem = item;
            }
        }
        if (defaultItem == null) {
            defaultItem = items.get(0);
        }

        items.sort((item, t1) -> item.getLabel().compareTo(t1.getLabel()));

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(SelectParameter.Builder.builder() //
                .name(NEW_PATTERN) //
                .items(items) //
                .item("custom", CUSTOM_PATTERN_PARAMETER) //
                .defaultValue(defaultItem.getValue()) //
                .build());

        return parameters;
    }

    /**
     * Get the new pattern from parameters.
     *
     * @param parameters the parameters map
     * @return a DatePattern object representing the pattern
     */
    default DatePattern getDateFormat(Map<String, String> parameters) {
        String pattern = "custom".equals(parameters.get(NEW_PATTERN)) ? parameters.get(CUSTOM_PATTERN) : parameters.get(NEW_PATTERN);
        try {
            if (StringUtils.isEmpty(pattern)) {
                throw new IllegalArgumentException();
            }
            return new DatePattern(pattern);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("pattern '" + pattern + "' is not a valid date pattern", iae);
        }
    }

    default void compileDatePattern(ActionContext actionContext) {
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            try {
                actionContext.get(COMPILED_DATE_PATTERN, p -> getDateFormat(actionContext.getParameters()));
            } catch (IllegalArgumentException e) {
                // Nothing to do, when pattern is invalid, cancel action.
                actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }
        }
    }

}
