//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import static org.talend.dataprep.i18n.ActionsBundle.choice;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    public static final String NEW_PATTERN = "new_pattern"; //$NON-NLS-1$

    /**
     * The parameter object for the custom new pattern.
     */
    public static final String CUSTOM_PATTERN = "custom_date_pattern"; //$NON-NLS-1$

    /**
     * Key to store compiled pattern in action context.
     */
    public static final String COMPILED_DATE_PATTERN = "compiled_datePattern";

    /**
     * @return the Parameters to display for the date related action.
     */
    protected List<Parameter> getParametersForDatePattern(Locale locale) {
        HashMap<String, String> datePatterns = loadDatePatterns();

        SelectParameter.SelectParameterBuilder selectParamBuilder = SelectParameter.selectParameter(locale).name(NEW_PATTERN);

        String defaultItem = null;
        for (Map.Entry<String, String> datePatternEntry : datePatterns.entrySet()) {
            String key = datePatternEntry.getKey();
            String value = datePatternEntry.getValue();

            selectParamBuilder.constant(value, choice(this, locale, key));

            if ("ISO".equals(key)){
                defaultItem = value;
            }
            if (defaultItem == null) {
                defaultItem = value;
            }
        }

        SelectParameter custom = selectParamBuilder //
                .item("custom", "custom", buildCustomPatternParam(locale)) //
                .defaultValue(defaultItem) //
                .build(this);
        custom.getItems().sort(compareOnLabelWithCustomLast());

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(custom);
        return parameters;
    }

    private Comparator<Item> compareOnLabelWithCustomLast() {
        return new Comparator<Item>() {

            private final Comparator<Item> labelComparator = Comparator.comparing(Item::getLabel);

            @Override
            public int compare(Item o1, Item o2) {
                if (o1.getValue().equals("custom")) {
                    return Integer.MAX_VALUE;
                } else if (o2.getValue().equals("custom")) {
                    return Integer.MIN_VALUE;
                }
                return labelComparator.compare(o1, o2);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, String> loadDatePatterns() {
        HashMap<String, String> datePatterns;
        try {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(getClass().getResourceAsStream("date_patterns.properties"), StandardCharsets.UTF_8));
            datePatterns = new HashMap(properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return datePatterns;
    }

    /**
     * The parameter object for the custom new pattern.
     */
    private Parameter buildCustomPatternParam(Locale locale) {
        return Parameter.parameter(locale).setName(CUSTOM_PATTERN)
                .setType(ParameterType.STRING)
                .setDefaultValue(EMPTY)
                .setCanBeBlank(false)
                .build(this);
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
    public String getCategory(Locale locale) {
        return ActionCategory.DATE.getDisplayName(locale);
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
