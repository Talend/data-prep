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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    protected static final String COUNTRY_NUMBER = "country_number";

    protected static final String COUNTRY_CODE_ISO3 = "country_code_iso3";

    protected static final String COUNTRY_CODE_ISO2 = "country_code_iso2";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = true;

    private static final String NEW_COLUMN_SEPARATOR = "_in_";

    private static final String NOT_ASCII_CHAR_PATTERN = "[^\\p{ASCII}]";

    // Persistent map of country names in case of country name for input
    private static Map<String, Integer> countryNames;

    private List<String> columnType;

    public CountryConverter() {
        // nothing to do here
    }

    private CountryConverter(List<String> columnType) {
        this.columnType = columnType;
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        ActionsUtils.AdditionalColumn newColumn = ActionsUtils.additionalColumn()
                .withName(context.getColumnName() + NEW_COLUMN_SEPARATOR + context.getParameters().get(TO_UNIT_PARAMETER));

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

        SelectParameter.SelectParameterBuilder builder = selectParameter(locale).item(COUNTRY_NAME, COUNTRY_NAME)
                .item(COUNTRY_CODE_ISO2, COUNTRY_CODE_ISO2).item(COUNTRY_CODE_ISO3, COUNTRY_CODE_ISO3)
                .item(COUNTRY_NUMBER, COUNTRY_NUMBER).canBeBlank(false).name(FROM_UNIT_PARAMETER);

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

        SelectParameter.SelectParameterBuilder secondBuilder = selectParameter(locale)
                .item(ENGLISH_COUNTRY_NAME, ENGLISH_COUNTRY_NAME).item(FRENCH_COUNTRY_NAME, FRENCH_COUNTRY_NAME)
                .item(COUNTRY_CODE_ISO2, COUNTRY_CODE_ISO2).item(COUNTRY_CODE_ISO3, COUNTRY_CODE_ISO3)
                .item(COUNTRY_NUMBER, COUNTRY_NUMBER).canBeBlank(false).name(TO_UNIT_PARAMETER);
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
        final String targetColumnId = ActionsUtils.getTargetColumnId(context);
        final String columnValue = row.get(columnId);

        int countryId = -1;
        CountryCode result;
        String countryCode;
        final String fromParameter = context.getParameters().get(FROM_UNIT_PARAMETER);

        switch (fromParameter) {
        case COUNTRY_NAME:
        case ENGLISH_COUNTRY_NAME:
        case FRENCH_COUNTRY_NAME:
            if (countryNames == null) {
                initializeCountryNameMap();
            }
            if (countryNames.containsKey(
                    Normalizer.normalize(columnValue.toLowerCase(), Normalizer.Form.NFD).replaceAll(NOT_ASCII_CHAR_PATTERN, StringUtils.EMPTY))) {
                countryId = countryNames.get(
                        Normalizer.normalize(columnValue.toLowerCase(), Normalizer.Form.NFD).replaceAll(NOT_ASCII_CHAR_PATTERN, StringUtils.EMPTY));
            }
            break;
        case COUNTRY_CODE_ISO2:
            countryCode = columnValue.toUpperCase();
            if (CountryCode.getByAlpha2Code(countryCode) != null) {
                countryId = CountryCode.getByAlpha2Code(countryCode).getNumeric();
            }
            break;
        case COUNTRY_CODE_ISO3:
            countryCode = columnValue.toUpperCase();
            if (CountryCode.getByAlpha3Code(countryCode) != null) {
                countryId = CountryCode.getByAlpha3Code(countryCode).getNumeric();
            }
            break;
        case COUNTRY_NUMBER:
            if (NumericHelper.isBigDecimal(columnValue)) {
                countryId = BigDecimalParser.toBigDecimal(columnValue).intValue();
            }
            break;
        default:
            // theoretically impossible case
            throw new IllegalArgumentException("Parameter value '" + fromParameter + "' is not supported.");
        }

        if (countryId != -1) {
            result = CountryCode.getByCode(countryId);
            String toParameter = context.getParameters().get(TO_UNIT_PARAMETER);
            Locale tempLocale;
            switch (toParameter) {
            case ENGLISH_COUNTRY_NAME:
                tempLocale = new Locale(StringUtils.EMPTY, result.getAlpha2());
                row.set(targetColumnId, tempLocale.getDisplayCountry(Locale.ENGLISH));
                break;
            case FRENCH_COUNTRY_NAME:
                tempLocale = new Locale(StringUtils.EMPTY, result.getAlpha2());
                row.set(targetColumnId, tempLocale.getDisplayCountry(Locale.FRENCH));
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
            default:
                // theoretically impossible case
                throw new IllegalArgumentException("Parameter value '" + toParameter + "' is not supported.");
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

    /**
     * This method will initialize the map of country names
     * with English and French country names as key and there country numbers as value.
     * 2 different sources are used to construct this map :
     * - one for English Country name which has a size of 270 entries CountryCode.values() (lib neovisionaries.i18n)
     * - one for French Country name which has a size of 250 entries Locale.getISOCountries() (lib java.util)
     * So some country names will not be transformed.
     * Moreover DQ use another source to compute invalid/valid entries, so some valid entries for DQ will not be transformed too.
     *
     * We transform name in order to use normalize key (without accent) and to be case insensitive.
     */
    private static void initializeCountryNameMap() {
        countryNames = new HashMap<>();

        // put country names in english
        for (CountryCode countryCode : CountryCode.values()) {
            if (countryCode != null && countryCode.getAssignment().equals(CountryCode.Assignment.OFFICIALLY_ASSIGNED)) {
                countryNames.put(Normalizer.normalize(countryCode.getName().toLowerCase(), Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", ""), countryCode.getNumeric());
            }
        }
        // put country names in french
        for (String countryCode : Locale.getISOCountries()) {
            if (countryCode != null) {
                Locale obj = new Locale(StringUtils.EMPTY, countryCode);
                String countryName = obj.getDisplayCountry(Locale.FRENCH);
                int countryNumber = CountryCode.getByAlpha2Code(countryCode).getNumeric();
                countryNames.put(
                        Normalizer.normalize(countryName.toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""),
                        countryNumber);
            }
        }
    }

}
