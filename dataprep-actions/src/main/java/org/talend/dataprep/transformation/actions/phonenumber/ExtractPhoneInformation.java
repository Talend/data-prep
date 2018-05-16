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
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.DE_REGION_CODE;
import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.FR_REGION_CODE;
import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.UK_REGION_CODE;
import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.US_REGION_CODE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.standardization.phone.PhoneNumberHandlerBase;

import com.google.i18n.phonenumbers.Phonenumber;

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

    /** Language name parameter */
    public static final String LANGUAGE = "LANGUAGE";

    /** Key for the locale value in context */
    private static String LOCALE = "LOCALE";

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
        return true;
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
        context.get(LOCALE, p -> new Locale(context.getParameters().get(LANGUAGE)));
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

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String originalValue = row.get(columnId);

        String regionCodeFromDomain = (!row.isInvalid(columnId)) ? context.get(REGION_CODE_FROM_DOMAIN) : null;
        Locale localeFromDomain = context.get(LOCALE);

        // Set the values in newly created columns
        if (StringUtils.isNotEmpty(originalValue)) {
            Phonenumber.PhoneNumber phoneNumber = PhoneNumberHandlerBase.parseToPhoneNumber(originalValue, regionCodeFromDomain);
            if (phoneNumber != null) {
                setPhoneInformations(row, context, phoneNumber, localeFromDomain);
            } else {
                setEmptyPhoneInformations(row, context);
            }
        } else {
            setEmptyPhoneInformations(row, context);
        }
    }

    private void setEmpty(DataSetRow row, ActionContext context, String columnName) {
        if (Boolean.valueOf(context.getParameters().get(columnName))) {
            final String typeColumn = ActionsUtils.getTargetColumnIds(context).get(columnName);
            row.set(typeColumn, "");
        }
    }

    private void setEmptyPhoneInformations(DataSetRow row, ActionContext context) {
        setEmpty(row, context, TYPE);
        setEmpty(row, context, COUNTRY);
        setEmpty(row, context, REGION);
        setEmpty(row, context, GEOCODER);
        setEmpty(row, context, CARRIER);
        setEmpty(row, context, TIME_ZONE);
    }

    private void setPhoneInformations(DataSetRow row, ActionContext context, Phonenumber.PhoneNumber phoneNumber, Locale localeFromDomain) {
        setSpecificPhoneInformation(row, context,TYPE, () -> PhoneNumberHandlerBase.getPhoneNumberType(phoneNumber).getName());
        setSpecificPhoneInformation(row, context,COUNTRY, () -> String.valueOf(PhoneNumberHandlerBase.getCountryCodeForPhoneNumber(phoneNumber)));
        setSpecificPhoneInformation(row, context,REGION, () -> PhoneNumberHandlerBase.extractRegionCode(phoneNumber));
        setSpecificPhoneInformation(row, context,GEOCODER, () -> PhoneNumberHandlerBase.getGeocoderDescriptionForNumber(phoneNumber, localeFromDomain));
        setSpecificPhoneInformation(row, context,CARRIER, () -> PhoneNumberHandlerBase.getCarrierNameForNumber(phoneNumber, localeFromDomain));
        setSpecificPhoneInformation(row, context,TIME_ZONE, () -> PhoneNumberHandlerBase
                .getTimeZonesForNumber(phoneNumber, false)
                .stream()
                .collect(Collectors.joining(",")));
    }

    private void setSpecificPhoneInformation(DataSetRow row, ActionContext context, String information, Supplier<String> supplier) {
        if (Boolean.valueOf(context.getParameters().get(information))) {
            final String carrierColumn = ActionsUtils.getTargetColumnIds(context).get(information);
            final String carrier = supplier.get();
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

        List<Locale> locales = Stream
                .of(Locale.CHINESE, Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, //
                        Locale.ITALIAN, Locale.JAPANESE, Locale.KOREAN, Locale.forLanguageTag("es")) //
                .sorted(Comparator.comparing(o -> o.getDisplayLanguage(locale))) //
                .collect(Collectors.toList());

        SelectParameter.SelectParameterBuilder builder = selectParameter(locale).name(LANGUAGE);
        for (Locale currentLocale : locales) {
            builder = builder.constant(currentLocale.getLanguage(), currentLocale.getDisplayLanguage(locale));
        }
        builder = builder.defaultValue(Locale.ENGLISH.getLanguage());

        parameters.add(builder.build(this));
        return parameters;
    }
}
