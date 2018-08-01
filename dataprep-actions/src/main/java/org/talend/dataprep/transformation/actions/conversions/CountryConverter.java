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
package org.talend.dataprep.transformation.actions.conversions;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.talend.dataprep.api.type.Type.NUMERIC;
import static org.talend.dataprep.api.type.Type.STRING;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.SemanticDomain;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.util.NumericHelper;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

import com.neovisionaries.i18n.CountryCode;

@Action(CountryConverter.ACTION_NAME)
public class CountryConverter extends AbstractActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
    public static final String ACTION_NAME = "country_converter";

    protected static final String FROM_UNIT_PARAMETER = "from_unit";

    protected static final String TO_UNIT_PARAMETER = "to_unit";

    protected static final String COUNTRY_NAME = "country_name";

    protected static final String ENGLISH_COUNTRY_NAME = "english_country_name";

    protected static final String FRENCH_COUNTRY_NAME = "french_country_name";

    protected static final String COUNTRY_CODE_ISO2 = "country_code_iso2";

    protected static final String COUNTRY_CODE_ISO3 = "country_code_iso3";

    protected static final String COUNTRY_NUMBER = "country_number";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = true;

    private static final String NEW_COLUMN_SEPARATOR = "_in_";

    private List<String> columnType;

    private static final Dictionary trieRoot = new Dictionary();

    private SelectParameter.SelectParameterBuilder secondBuilder;

    public CountryConverter() {
        // nothing to do here
    }

    private CountryConverter(List<String> columnType) {
        this.columnType = columnType;
    }

    // Initialise trie tree only at the first use of the action.
    static {
        for (CountryCode countryCode : CountryCode.values()) {
            if (countryCode.getAssignment().equals(CountryCode.Assignment.OFFICIALLY_ASSIGNED)) {
                trieRoot.insert(countryCode.getName(), countryCode.getNumeric());
            }
        }
        for (String countryCode : Locale.getISOCountries()) {
            Locale obj = new Locale("", countryCode);
            String countryName = obj.getDisplayCountry(Locale.FRENCH);
            int countryNumber = CountryCode.getByAlpha2Code(countryCode).getNumeric();
            trieRoot.insert(countryName, countryNumber);
        }
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        ActionsUtils.AdditionalColumn newColumn = ActionsUtils.additionalColumn().withName(
                context.getColumnName() + NEW_COLUMN_SEPARATOR + context.getParameters().get(TO_UNIT_PARAMETER));

        if (context.getParameters().get(TO_UNIT_PARAMETER).equals(COUNTRY_NUMBER)) {
            newColumn.withType(NUMERIC);
        } else {
            newColumn.withType(STRING);
        }
        return singletonList(newColumn);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));

        SelectParameter.SelectParameterBuilder builder = selectParameter(locale)
                .item(COUNTRY_NAME, COUNTRY_NAME)
                .item(COUNTRY_CODE_ISO2, COUNTRY_CODE_ISO2)
                .item(COUNTRY_CODE_ISO3, COUNTRY_CODE_ISO3)
                .item(COUNTRY_NUMBER, COUNTRY_NUMBER)
                .canBeBlank(false)
                .name(FROM_UNIT_PARAMETER);

        if (columnType != null) {
            if (columnType.contains(SemanticCategoryEnum.COUNTRY_CODE_ISO2.getId())) {
                builder.defaultValue(COUNTRY_CODE_ISO2);
            } else if (columnType.contains(SemanticCategoryEnum.COUNTRY_CODE_ISO3.getId())) {
                builder.defaultValue(COUNTRY_CODE_ISO3);
            } else {
                builder.defaultValue(COUNTRY_NAME);
            }
        } else {
            builder.defaultValue(COUNTRY_NAME);
        }

        parameters.add(builder.build(this));

        secondBuilder = selectParameter(locale)
                .item(ENGLISH_COUNTRY_NAME, ENGLISH_COUNTRY_NAME)
                .item(FRENCH_COUNTRY_NAME, FRENCH_COUNTRY_NAME)
                .item(COUNTRY_CODE_ISO2, COUNTRY_CODE_ISO2)
                .item(COUNTRY_CODE_ISO3, COUNTRY_CODE_ISO3)
                .item(COUNTRY_NUMBER, COUNTRY_NUMBER)
                .canBeBlank(false)
                .name(TO_UNIT_PARAMETER);
        if (columnType != null) {
            if (columnType.contains(SemanticCategoryEnum.COUNTRY_CODE_ISO2.getId())
                    || columnType.contains(SemanticCategoryEnum.COUNTRY_CODE_ISO3.getId())) {
                secondBuilder.defaultValue(ENGLISH_COUNTRY_NAME);
            } else {
                secondBuilder.defaultValue(COUNTRY_CODE_ISO2);
            }
        } else {
            secondBuilder.defaultValue(COUNTRY_CODE_ISO2);
        }

        parameters.add(secondBuilder.build(this));
        return parameters;
    }

    @Override
    public String getName() {
        return CountryConverter.ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.CONVERSIONS.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        final List<String> semanticCategories = Arrays.asList(SemanticCategoryEnum.COUNTRY.name(),
                SemanticCategoryEnum.COUNTRY_CODE_ISO2.name(), SemanticCategoryEnum.COUNTRY_CODE_ISO3.name());
        final String domain = column.getDomain().toUpperCase();
        return Type.NUMERIC.isAssignableFrom(column.getType()) || semanticCategories.contains(domain);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return singleton(Behavior.VALUES_COLUMN);
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String columnValue = row.get(columnId);
        final String targetColumnId = ActionsUtils.getTargetColumnId(context);
        int countryId = -1;
        CountryCode result;

        switch (context.getParameters().get(FROM_UNIT_PARAMETER)) {
        case COUNTRY_NAME:
            countryId = trieRoot.search(columnValue);
            break;
        case COUNTRY_CODE_ISO2:
            if (CountryCode.getByAlpha2Code(columnValue) != null) {
                countryId = CountryCode.getByAlpha2Code(columnValue).getNumeric();
            }
            break;
        case COUNTRY_CODE_ISO3:
            if (CountryCode.getByAlpha3Code(columnValue) != null) {
                countryId = CountryCode.getByAlpha3Code(columnValue).getNumeric();
            }
            break;
        case COUNTRY_NUMBER:
            if (NumericHelper.isBigDecimal(columnValue)) {
                countryId = BigDecimalParser.toBigDecimal(columnValue).intValue();
            }
            break;
        }

        if (countryId != -1) {
            result = CountryCode.getByCode(countryId);
            switch (context.getParameters().get(TO_UNIT_PARAMETER)) {
            case ENGLISH_COUNTRY_NAME:
                row.set(targetColumnId, result.getName());
                break;
            case FRENCH_COUNTRY_NAME:
                Locale tempLocale = new Locale("", result.getAlpha2());
                row.set(targetColumnId, tempLocale.getDisplayLanguage(Locale.FRENCH));
                break;
            case COUNTRY_CODE_ISO2:
                row.set(targetColumnId, result.getAlpha2());
                break;
            case COUNTRY_CODE_ISO3:
                row.set(targetColumnId, result.getAlpha3());
                break;
            case COUNTRY_NUMBER:
                row.set(targetColumnId, Integer.toString(result.getNumeric()));
                break;
            }
        } else {
            row.set(targetColumnId, StringUtils.EMPTY);
        }
    }

    @Override
    public ActionDefinition adapt(ColumnMetadata column) {
        List<String> semanticIds = new ArrayList<>();
        for (SemanticDomain semanticDomain : column.getSemanticDomains()) {
            semanticIds.add(semanticDomain.getId());
        }
        return new CountryConverter(semanticIds);
    }

    // Trie tree
    static class Dictionary {

        private HashMap<Character, Node> roots = new HashMap<>();

        /**
         * Search through the dictionary for a word.
         * 
         * @param string The word to search for.
         * @return Whether or not the word exists in the dictionary.
         */
        public int search(String string) {
            if (roots.containsKey(string.charAt(0))) {
                if (string.length() == 1) {
                    return roots.get(string.charAt(0)).value;
                }
                return searchFor(string.substring(1), roots.get(string.charAt(0)));
            } else {
                return -1;
            }
        }

        /**
         * Insert a word into the dictionary.
         * 
         * @param string The word to insert.
         */
        public void insert(String string, int value) {
            if (!roots.containsKey(string.charAt(0))) {
                roots.put(string.charAt(0), new Node());
            }

            insertWord(string.substring(1), roots.get(string.charAt(0)), value);
        }

        // Adds a new word to the trie tree.
        private void insertWord(String string, Node node, int value) {
            final Node nextChild;
            if (node.children.containsKey(string.charAt(0))) {
                nextChild = node.children.get(string.charAt(0));
            } else {
                nextChild = new Node();
                node.children.put(string.charAt(0), nextChild);
            }

            if (string.length() == 1) {
                nextChild.endOfWord = true;
                nextChild.value = value;
            } else {
                insertWord(string.substring(1), nextChild, value);
            }
        }

        // Recursive method that searches through the Trie Tree to find the value.
        private int searchFor(String string, Node node) {
            if (string.length() == 0) {
                return node.value;
            }

            if (node.children.containsKey(string.charAt(0))) {
                return searchFor(string.substring(1), node.children.get(string.charAt(0)));
            } else {
                return -1;
            }
        }
    }

    static class Node {

        protected Node parent;

        protected Boolean endOfWord = false; // Does this Node mark the end of a particular word?

        protected int value = -1;

        protected HashMap<Character, Node> children = new HashMap<Character, Node>();
    }

}
