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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * BLABLA
 */
@Action(ExtractPhoneInformation.EXTRACT_PHONE_INFORMATION)
public class ExtractPhoneInformation extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String EXTRACT_PHONE_INFORMATION = "extract_phone_information"; //$NON-NLS-1$

    /**
     * The phone type suffix.
     */
    private static final String TYPE_SUFFIX = "_type"; //$NON-NLS-1$

    private static final String TYPE = "phone_type"; //$NON-NLS-1$

    /**
     * The region suffix.
     */
    private static final String REGION_SUFFIX = "_region"; //$NON-NLS-1$

    private static final String REGION = "phone_region"; //$NON-NLS-1$

    /**
     * The country suffix.
     */
    private static final String COUNTRY_SUFFIX = "_country"; //$NON-NLS-1$

    private static final String COUNTRY = "phone_country"; //$NON-NLS-1$

    /**
     * The Time Zone suffix.
     */
    private static final String TIME_ZONE_SUFFIX = "_timezone"; //$NON-NLS-1$

    private static final String TIME_ZONE = "phone_timezone"; //$NON-NLS-1$

    /**
     * The Geocoder description suffix.
     */
    private static final String GEOCODER_SUFFIX = "_geographicArea"; //$NON-NLS-1$

    private static final String GEOCODER = "phone_geographicArea"; //$NON-NLS-1$

    /**
     * The Carrier name description suffix.
     */
    private static final String CARRIER_SUFFIX = "_carrierName"; //$NON-NLS-1$

    private static final String CARRIER = "phone_carrierName"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractPhoneInformation.class);

    @Override
    public String getName() {
        return EXTRACT_PHONE_INFORMATION;
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
        if (Boolean.valueOf(context.getParameters().get(TYPE)))
            additionalColumns
                    .add(ActionsUtils.additionalColumn().withKey(TYPE).withName(column.getName() + TYPE_SUFFIX));
        if (Boolean.valueOf(context.getParameters().get(REGION)))
            additionalColumns
                    .add(ActionsUtils.additionalColumn().withKey(REGION).withName(column.getName() + REGION_SUFFIX));
        if (Boolean.valueOf(context.getParameters().get(COUNTRY)))
            additionalColumns
                    .add(ActionsUtils.additionalColumn().withKey(COUNTRY).withName(column.getName() + COUNTRY_SUFFIX));
        if (Boolean.valueOf(context.getParameters().get(TIME_ZONE)))
            additionalColumns.add(
                    ActionsUtils.additionalColumn().withKey(TIME_ZONE).withName(column.getName() + TIME_ZONE_SUFFIX));
        if (Boolean.valueOf(context.getParameters().get(GEOCODER)))
            additionalColumns.add(
                    ActionsUtils.additionalColumn().withKey(GEOCODER).withName(column.getName() + GEOCODER_SUFFIX));
        if (Boolean.valueOf(context.getParameters().get(CARRIER)))
            additionalColumns
                    .add(ActionsUtils.additionalColumn().withKey(CARRIER).withName(column.getName() + CARRIER_SUFFIX));

        ActionsUtils.createNewColumn(context, additionalColumns);
        System.out.println("size after compile : " + context.getRowMetadata().size());
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        System.out.println("size before apply : " + context.getRowMetadata().size());
        final String columnId = context.getColumnId();
        final String originalValue = row.get(columnId);
        // Set the values in newly created columns
        if (originalValue == null || row.isInvalid(columnId)) {
            return;
        }

        String domain = row.getRowMetadata().getById(columnId).getDomain();
        String regionCodeFromDomain;
        Locale localeFromDomain;
        switch (domain) {
        case "FR_PHONE":
            regionCodeFromDomain = FR_REGION_CODE;
            localeFromDomain = Locale.FRANCE;
            break;
        case "DE_PHONE":
            regionCodeFromDomain = DE_REGION_CODE;
            localeFromDomain = Locale.GERMANY;
            break;
        case "US_PHONE":
            regionCodeFromDomain = US_REGION_CODE;
            localeFromDomain = Locale.US;
            break;
        case "UK_PHONE":
            regionCodeFromDomain = UK_REGION_CODE;
            localeFromDomain = Locale.UK;
            break;
        default:
            LOGGER.error("Unsupported domain " + domain);
            return;
        }

        final String typeColumn = ActionsUtils.getTargetColumnIds(context).get(TYPE);
        final PhoneNumberTypeEnum type = PhoneNumberHandlerBase.getPhoneNumberType(originalValue, regionCodeFromDomain);
        row.set(typeColumn, type.getName());

        final String regionColumn = ActionsUtils.getTargetColumnIds(context).get(REGION);
        final String regionCode = PhoneNumberHandlerBase.extractRegionCode(originalValue, regionCodeFromDomain);
        row.set(regionColumn, regionCode);

        final String countryColumn = ActionsUtils.getTargetColumnIds(context).get(COUNTRY);
        final int country = PhoneNumberHandlerBase.getCountryCodeForRegion(regionCodeFromDomain);
        row.set(countryColumn, String.valueOf(country));

        final String timezoneColumn = ActionsUtils.getTargetColumnIds(context).get(TIME_ZONE);
        final String timezones = PhoneNumberHandlerBase
                .getTimeZonesForNumber(originalValue, regionCodeFromDomain, false)
                .stream()
                .collect(Collectors.joining(","));
        row.set(timezoneColumn, timezones);

        final String geocoderColumn = ActionsUtils.getTargetColumnIds(context).get(GEOCODER);
        final String geocoder = PhoneNumberHandlerBase.getGeocoderDescriptionForNumber(originalValue,
                regionCodeFromDomain, localeFromDomain);
        row.set(geocoderColumn, geocoder);

        final String carrierColumn = ActionsUtils.getTargetColumnIds(context).get(CARRIER);
        final String carrier =
                PhoneNumberHandlerBase.getCarrierNameForNumber(originalValue, regionCodeFromDomain, localeFromDomain);
        row.set(carrierColumn, carrier);

    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(parameter(locale).setName(TYPE).setType(BOOLEAN).setDefaultValue(true).build(this));
        parameters.add(parameter(locale).setName(REGION).setType(BOOLEAN).setDefaultValue(true).build(this));
        parameters.add(parameter(locale).setName(COUNTRY).setType(BOOLEAN).setDefaultValue(true).build(this));
        parameters.add(parameter(locale).setName(TIME_ZONE).setType(BOOLEAN).setDefaultValue(true).build(this));
        parameters.add(parameter(locale).setName(GEOCODER).setType(BOOLEAN).setDefaultValue(true).build(this));
        parameters.add(parameter(locale).setName(CARRIER).setType(BOOLEAN).setDefaultValue(true).build(this));

        // parameters.add(selectParameter(locale) //
        // .name(MODE_PARAMETER) //
        // .item(CONSTANT_MODE, CONSTANT_MODE, //
        // selectParameter(locale).name(REGIONS_PARAMETER_CONSTANT_MODE).canBeBlank(true) //
        // .item(US_REGION_CODE, US_REGION_CODE) //
        // .item(FR_REGION_CODE, FR_REGION_CODE) //
        // .item(UK_REGION_CODE, UK_REGION_CODE) //
        // .item(DE_REGION_CODE, DE_REGION_CODE) //
        // .item(OTHER_REGION_TO_BE_SPECIFIED, OTHER_REGION_TO_BE_SPECIFIED,
        // parameter(locale).setName(MANUAL_REGION_PARAMETER_STRING)
        // .setType(STRING)
        // .setDefaultValue(EMPTY)
        // .build(this)).defaultValue(US_REGION_CODE).build(this)) //
        // .defaultValue(CONSTANT_MODE).build(this));

        return parameters;
    }
}
