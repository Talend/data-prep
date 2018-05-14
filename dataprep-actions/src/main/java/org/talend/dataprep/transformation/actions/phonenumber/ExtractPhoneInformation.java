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

package org.talend.dataprep.transformation.actions.phonenumber;

import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.BOOLEAN;
import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.DE_REGION_CODE;
import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.FR_REGION_CODE;
import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.UK_REGION_CODE;
import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.US_REGION_CODE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.DE_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.FR_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.UK_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.US_PHONE;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.standardization.phone.PhoneNumberHandlerBase;
import org.talend.dataquality.standardization.phone.PhoneNumberTypeEnum;

/**
 * Action allowing to extract informations from a phone number (using the google phone library)
 */
@Action(ExtractPhoneInformation.ACTION_NAME)
public class ExtractPhoneInformation extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "extract_phone_information";

    /**
     * The phone type suffix.
     */
    private static final String TYPE_SUFFIX = "_type";

    private static final String TYPE = "phone_type";

    /**
     * The country suffix.
     */
    private static final String COUNTRY_SUFFIX = "_country";

    private static final String COUNTRY = "phone_country";

    /**
     * The region suffix.
     */
    private static final String REGION_SUFFIX = "_region";

    private static final String REGION = "phone_region";

    /**
     * The Geocoder description suffix.
     */
    private static final String GEOCODER_SUFFIX = "_geographicArea";

    private static final String GEOCODER = "phone_geographicArea";

    /**
     * The Carrier name description suffix.
     */
    private static final String CARRIER_SUFFIX = "_carrierName";

    private static final String CARRIER = "phone_carrierName";

    /**
     * The Time Zone suffix.
     */
    private static final String TIME_ZONE_SUFFIX = "_timezone";

    private static final String TIME_ZONE = "phone_timezone";

    private static final String REGION_CODE_FROM_DOMAIN = "region_from_domain";

    private static final String LOCALE_FROM_DOMAIN = "locale_from_domain";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.PHONE_NUMBER.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        final String domain = column.getDomain().toUpperCase();
        return Stream
                .of(US_PHONE, UK_PHONE, DE_PHONE, FR_PHONE) //
                .map(SemanticCategoryEnum::name) //
                .anyMatch(domain::equals);
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        final List<ActionsUtils.AdditionalColumn> additionalColumns = new ArrayList<>();
        final RowMetadata rowMetadata = context.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(context.getColumnId());
        addColumn(additionalColumns, column, context, TYPE, TYPE_SUFFIX);
        addColumn(additionalColumns, column, context, COUNTRY, COUNTRY_SUFFIX);
        addColumn(additionalColumns, column, context, REGION, REGION_SUFFIX);
        addColumn(additionalColumns, column, context, GEOCODER, GEOCODER_SUFFIX);
        addColumn(additionalColumns, column, context, CARRIER, CARRIER_SUFFIX);
        addColumn(additionalColumns, column, context, TIME_ZONE, TIME_ZONE_SUFFIX);

        ActionsUtils.createNewColumn(context, additionalColumns);

        SemanticCategoryEnum domainEnum = SemanticCategoryEnum.getCategoryById(column.getDomain());

        context.get(REGION_CODE_FROM_DOMAIN, p -> getRegionCodeFromDomain(domainEnum));
        context.get(LOCALE_FROM_DOMAIN, p -> getLocaleFromDomain(domainEnum));
    }

    private void addColumn(List<ActionsUtils.AdditionalColumn> additionalColumns, ColumnMetadata column, ActionContext context, String info, String infoSuffix) {
        if (Boolean.valueOf(context.getParameters().get(info)))
            additionalColumns
                    .add(ActionsUtils.additionalColumn().withKey(info).withName(column.getName() + infoSuffix));
    }

    private String getRegionCodeFromDomain(SemanticCategoryEnum domainEnum) {
        String region = null;
        switch (domainEnum) {
        case FR_PHONE:
            region = FR_REGION_CODE;
            break;
        case DE_PHONE:
            region = DE_REGION_CODE;
            break;
        case US_PHONE:
            region = US_REGION_CODE;
            break;
        case UK_PHONE:
            region = UK_REGION_CODE;
            break;
        default:
        }
        return region;
    }

    private Locale getLocaleFromDomain(SemanticCategoryEnum domainEnum) {
        Locale locale = null;
        switch (domainEnum) {
        case FR_PHONE:
            locale = Locale.FRANCE;
            break;
        case DE_PHONE:
            locale = Locale.GERMANY;
            break;
        case US_PHONE:
            locale = Locale.US;
            break;
        case UK_PHONE:
            locale = Locale.UK;
            break;
        default:
        }
        return locale;
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String originalValue = row.get(columnId);

        String regionCodeFromDomain = context.get(REGION_CODE_FROM_DOMAIN);
        Locale localeFromDomain = context.get(LOCALE_FROM_DOMAIN);

        // Set the values in newly created columns
        if (StringUtils.isNotEmpty(originalValue) && !row.isInvalid(columnId) && regionCodeFromDomain != null) {
            setPhoneType(row, context, originalValue, regionCodeFromDomain);
            setCountryRegion(row, context, regionCodeFromDomain);
            setPhoneRegion(row, context, originalValue, regionCodeFromDomain);
            setGeocoder(row, context, originalValue, regionCodeFromDomain, localeFromDomain);
            setCarrier(row, context, originalValue, regionCodeFromDomain, localeFromDomain);
            setTimezones(row, context, originalValue, regionCodeFromDomain);
        } else {
            setEmpty(row, context, TYPE);
            setEmpty(row, context, COUNTRY);
            setEmpty(row, context, REGION);
            setEmpty(row, context, GEOCODER);
            setEmpty(row, context, CARRIER);
            setEmpty(row, context, TIME_ZONE);
        }
    }

    private void setEmpty(DataSetRow row, ActionContext context, String columnName) {
        if (Boolean.valueOf(context.getParameters().get(columnName))) {
            final String typeColumn = ActionsUtils.getTargetColumnIds(context).get(columnName);
            row.set(typeColumn, "");
        }
    }

    private void setPhoneType(DataSetRow row, ActionContext context, String originalValue,
            String regionCodeFromDomain) {
        if (Boolean.valueOf(context.getParameters().get(TYPE))) {
            final String typeColumn = ActionsUtils.getTargetColumnIds(context).get(TYPE);
            final PhoneNumberTypeEnum type =
                    PhoneNumberHandlerBase.getPhoneNumberType(originalValue, regionCodeFromDomain);
            row.set(typeColumn, type.getName());
        }
    }

    private void setPhoneRegion(DataSetRow row, ActionContext context, String originalValue,
            String regionCodeFromDomain) {
        if (Boolean.valueOf(context.getParameters().get(REGION))) {
            final String regionColumn = ActionsUtils.getTargetColumnIds(context).get(REGION);
            final String regionCode = PhoneNumberHandlerBase.extractRegionCode(originalValue, regionCodeFromDomain);
            row.set(regionColumn, regionCode);
        }
    }

    private void setCountryRegion(DataSetRow row, ActionContext context, String regionCodeFromDomain) {
        if (Boolean.valueOf(context.getParameters().get(COUNTRY))) {
            final String countryColumn = ActionsUtils.getTargetColumnIds(context).get(COUNTRY);
            final int country = PhoneNumberHandlerBase.getCountryCodeForRegion(regionCodeFromDomain);
            row.set(countryColumn, String.valueOf(country));
        }
    }

    private void setTimezones(DataSetRow row, ActionContext context, String originalValue,
            String regionCodeFromDomain) {
        if (Boolean.valueOf(context.getParameters().get(TIME_ZONE))) {
            final String timezoneColumn = ActionsUtils.getTargetColumnIds(context).get(TIME_ZONE);
            final String timezones = PhoneNumberHandlerBase
                    .getTimeZonesForNumber(originalValue, regionCodeFromDomain, false)
                    .stream()
                    .collect(Collectors.joining(","));
            row.set(timezoneColumn, timezones);
        }
    }

    private void setGeocoder(DataSetRow row, ActionContext context, String originalValue, String regionCodeFromDomain,
            Locale localeFromDomain) {
        if (Boolean.valueOf(context.getParameters().get(GEOCODER))) {
            final String geocoderColumn = ActionsUtils.getTargetColumnIds(context).get(GEOCODER);
            final String geocoder = PhoneNumberHandlerBase.getGeocoderDescriptionForNumber(originalValue,
                    regionCodeFromDomain, localeFromDomain);
            row.set(geocoderColumn, geocoder);
        }
    }

    private void setCarrier(DataSetRow row, ActionContext context, String originalValue, String regionCodeFromDomain,
            Locale localeFromDomain) {
        if (Boolean.valueOf(context.getParameters().get(CARRIER))) {
            final String carrierColumn = ActionsUtils.getTargetColumnIds(context).get(CARRIER);
            final String carrier = PhoneNumberHandlerBase.getCarrierNameForNumber(originalValue, regionCodeFromDomain,
                    localeFromDomain);
            row.set(carrierColumn, carrier);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS, Behavior.NEED_STATISTICS_INVALID);
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.addAll(
                Stream.of(TYPE, COUNTRY, REGION, GEOCODER, CARRIER, TIME_ZONE)//
                .map(name -> parameter(locale).setName(name).setType(BOOLEAN).setDefaultValue(true).build(this))//
                .collect(Collectors.toList()));
        return parameters;
    }
}
