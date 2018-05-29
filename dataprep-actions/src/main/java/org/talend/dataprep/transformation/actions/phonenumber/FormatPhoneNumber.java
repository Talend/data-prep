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

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.COLUMN;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.actions.category.ScopeCategory.DATASET;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.CONSTANT_MODE;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.MODE_PARAMETER;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.OTHER_COLUMN_MODE;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.SELECTED_COLUMN_PARAMETER;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.DE_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.FR_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.UK_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.US_PHONE;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.AbstractMultiScopeAction;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.standardization.phone.PhoneNumberHandlerBase;

/**
 * Format a validated phone number to a specified format.
 */
@Action(FormatPhoneNumber.ACTION_NAME)
public class FormatPhoneNumber extends AbstractMultiScopeAction {
    /**
     * Action name.
     */
    public static final String ACTION_NAME = "format_phone_number"; //$NON-NLS-1$

    protected static final String NEW_COLUMN_SUFFIX = "_formatted";

    public static final String OTHER_REGION_TO_BE_SPECIFIED = "other_region";

    /**
     * the follow 4 types is provided to user selection on UI
     */
    public static final String TYPE_INTERNATIONAL = "international"; //$NON-NLS-1$

    public static final String TYPE_NATIONAL = "national"; //$NON-NLS-1$

    public static final String TYPE_E164 = "E164"; //$NON-NLS-1$

    public static final String TYPE_RFC3966 = "RFC3966"; //$NON-NLS-1$

    /**
     * a region code parameter
     */
    public static final String REGIONS_PARAMETER_CONSTANT_MODE = "region_code"; //$NON-NLS-1$

    /**
     * a manually input parameter of region code
     */
    static final String MANUAL_REGION_PARAMETER_STRING = "manual_region_string"; //$NON-NLS-1$

    /**
     * a parameter of format type
     */
    public static final String FORMAT_TYPE_PARAMETER = "format_type"; //$NON-NLS-1$

    private static final String PHONE_NUMBER_HANDLER_KEY = "phone_number_handler_helper"; //$NON-NLS-1$

    protected static final String US_REGION_CODE = "US";

    protected static final String FR_REGION_CODE = "FR";

    protected static final String UK_REGION_CODE = "GB";

    protected static final String DE_REGION_CODE = "DE";

    private static final Logger LOGGER = LoggerFactory.getLogger(FormatPhoneNumber.class);

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    public FormatPhoneNumber() {
        this(ScopeCategory.COLUMN);
    }

    public FormatPhoneNumber(ScopeCategory scope) {
        super(scope);
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
        if (context.getActionStatus() == OK) {
            try {
                context.get(PHONE_NUMBER_HANDLER_KEY, p -> new PhoneNumberHandlerBase());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                context.setActionStatus(CANCELED);
            }
        }
    }

    @Override
    public void apply(DataSetRow row, String columnId, String targetColumnId, ActionContext context) {
        final String possiblePhoneValue = row.get(columnId);
        if (StringUtils.isEmpty(possiblePhoneValue)) {
            row.set(targetColumnId, possiblePhoneValue);
            return;
        }

        final String regionCode = getRegionCode(context, row);

        final String formatedStr = formatIfValid(regionCode,
                context.getParameters().get(FORMAT_TYPE_PARAMETER), possiblePhoneValue);
        row.set(targetColumnId, formatedStr);
    }

    /**
     * When the phone is a valid phone number,format it as the specified form.
     *
     * @return the formatted phone number or the original value if cannot be formatted
     */
    private String formatIfValid(String regionParam, String formatType, String phone) {
        if (formatType == null || !PhoneNumberHandlerBase.isPossiblePhoneNumber(phone, regionParam)) {
            return phone;
        }
        switch (formatType) {
            case TYPE_INTERNATIONAL:
                return PhoneNumberHandlerBase.formatInternational(phone, regionParam);
            case TYPE_NATIONAL:
                return PhoneNumberHandlerBase.formatNational(phone, regionParam);
            case TYPE_E164:
                return PhoneNumberHandlerBase.formatE164(phone, regionParam);
            case TYPE_RFC3966:
                return PhoneNumberHandlerBase.formatRFC396(phone, regionParam);
            default:
                return phone;
        }
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        if (ScopeCategory.COLUMN.equals(scope)) {
            parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));
        }
        parameters.add(selectParameter(locale) //
                .name(MODE_PARAMETER) //
                .item(OTHER_COLUMN_MODE, OTHER_COLUMN_MODE, //
                        parameter(locale).setName(SELECTED_COLUMN_PARAMETER)
                                .setType(COLUMN)
                                .setDefaultValue(EMPTY)
                                .setCanBeBlank(false)
                                .build(this)) //
                .item(CONSTANT_MODE, CONSTANT_MODE, //
                        selectParameter(locale).name(REGIONS_PARAMETER_CONSTANT_MODE).canBeBlank(true) //
                                .item(US_REGION_CODE, US_REGION_CODE) //
                                .item(FR_REGION_CODE, FR_REGION_CODE) //
                                .item(UK_REGION_CODE, UK_REGION_CODE) //
                                .item(DE_REGION_CODE, DE_REGION_CODE) //
                                .item(OTHER_REGION_TO_BE_SPECIFIED, OTHER_REGION_TO_BE_SPECIFIED,
                                        parameter(locale).setName(MANUAL_REGION_PARAMETER_STRING)
                                                .setType(STRING)
                                                .setDefaultValue(EMPTY)
                                                .build(this)).defaultValue(US_REGION_CODE).build(this)) //
                .defaultValue(CONSTANT_MODE).build(this));

        parameters.add(selectParameter(locale).name(FORMAT_TYPE_PARAMETER) //
                .item(TYPE_INTERNATIONAL, TYPE_INTERNATIONAL) //
                .item(TYPE_NATIONAL, TYPE_NATIONAL) //
                .item(TYPE_E164) //
                .item(TYPE_RFC3966) //
                .defaultValue(TYPE_INTERNATIONAL).build(this));
        return parameters;
    }

    private String getRegionCode(ActionContext context, DataSetRow row) {
        final Map<String, String> parameters = context.getParameters();
        final String regionParam;
        switch (parameters.get(OtherColumnParameters.MODE_PARAMETER)) {
            case CONSTANT_MODE:
                final String constantModeParameter = parameters.get(REGIONS_PARAMETER_CONSTANT_MODE);
            if (OTHER_REGION_TO_BE_SPECIFIED.equals(constantModeParameter)) {
                    regionParam = parameters.get(MANUAL_REGION_PARAMETER_STRING);
                } else {
                    regionParam = constantModeParameter;
                }
                break;
            case OTHER_COLUMN_MODE:
                final ColumnMetadata selectedColumn = context.getRowMetadata()
                        .getById(parameters.get(OtherColumnParameters.SELECTED_COLUMN_PARAMETER));
                regionParam = row.get(selectedColumn.getId());
                break;
            default:
                regionParam = Locale.getDefault().getCountry();
                break;
        }
        return regionParam;
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.PHONE_NUMBER.getDisplayName(locale);
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(ActionsUtils.additionalColumn().withName(context.getColumnName() + NEW_COLUMN_SUFFIX));
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        final String domain = column.getDomain().toUpperCase();
        return Stream.of(PHONE, US_PHONE, UK_PHONE, DE_PHONE, FR_PHONE) //
                .map(SemanticCategoryEnum::name) //
                .anyMatch(domain::equals);
    }

    @Override
    public ActionDefinition adapt(ScopeCategory scope) {
        return new FormatPhoneNumber(scope);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(DATASET.equals(scope) ? Behavior.VALUES_ALL : Behavior.VALUES_COLUMN);
    }

}
