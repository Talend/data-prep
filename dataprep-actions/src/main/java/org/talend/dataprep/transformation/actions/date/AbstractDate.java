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
import static org.talend.dataprep.api.type.Type.DATE;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Item;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

public abstract class AbstractDate extends AbstractActionMetadata {

    /**
     * Name of the new date pattern parameter.
     */
    public static String NEW_PATTERN = "new_pattern"; //$NON-NLS-1$

    /**
     * The parameter object for the custom new pattern.
     */
    public static String CUSTOM_PATTERN = "custom_date_pattern"; //$NON-NLS-1$

    /**
     * Key to store compiled pattern in action context.
     */
    public static String COMPILED_DATE_PATTERN = "compiled_datePattern";

    /**
     * The parameter object for the custom new pattern.
     */
    Parameter CUSTOM_PATTERN_PARAMETER = new Parameter(CUSTOM_PATTERN, ParameterType.STRING, EMPTY, false, false);

    /**
     * @return the Parameters to display for the date related action.
     */
    protected List<Parameter> getParametersForDatePattern() {

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
                    .label(key)
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
    DatePattern getDateFormat(Map<String, String> parameters) {
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

    void compileDatePattern(ActionContext actionContext) {
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            try {
                actionContext.get(COMPILED_DATE_PATTERN, p -> getDateFormat(actionContext.getParameters()));
            } catch (IllegalArgumentException e) {
                // Nothing to do, when pattern is invalid, cancel action.
                actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }
        }
    }

    @Override
    public String getCategory() {
        return ActionCategory.DATE.getDisplayName();
    }

    /**
     * Only works on 'date' columns.
     * @param column The column to check, returns <code>true</code> only for date columns.
     */
    @Override
    public boolean acceptField(ColumnMetadata column) {
        final String domain = column.getDomain().toUpperCase();
        return DATE.equals(Type.get(column.getType())) || SemanticCategoryEnum.DATE.name().equals(domain);
    }

}
