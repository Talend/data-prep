// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.CONSTANT_MODE;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.OTHER_COLUMN_MODE;

import java.util.*;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.standardization.phone.PhoneNumberHandlerBase;

/**
 * Format a validated phone number to a specified format.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + FormatPhoneNumber.ACTION_NAME)
public class FormatPhoneNumber extends AbstractActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
    public static final String ACTION_NAME = "format_phone_number"; //$NON-NLS-1$

    /** a region code parameter */
    static final String REGIONS_PARAMETER_CONSTANT_MODE = "region_code"; //$NON-NLS-1$

    /** a manually input parameter of region code */
    static final String MANUAL_REGION_PARAMETER_STRING = "manual_region_string"; //$NON-NLS-1$

    /** a parameter of format type */
    static final String FORMAT_TYPE_PARAMETER = "format_type"; //$NON-NLS-1$

    private static final String PHONE_NUMBER_HANDLER_KEY = "phone_number_handler_helper";//$NON-NLS-1$

    private static final String US_REGION_CODE = "US";

    private static final String FR_REGION_CODE = "FR";

    private static final String UK_REGION_CODE = "UK";

    private static final String DE_REGION_CODE = "DE";

    static final String OTHER_REGION_TO_BE_SPECIFIED = "other (region)";

    private static final Logger LOGGER = LoggerFactory.getLogger(FormatPhoneNumber.class);

    /** the follow 4 types is provided to user selection on UI */
    static final String TYPE_INTERNATIONAL = "International"; //$NON-NLS-1$

    static final String TYPE_NATIONAL = "National"; //$NON-NLS-1$

    static final String TYPE_E164 = "E164"; //$NON-NLS-1$

    static final String TYPE_RFC3966 = "RFC3966"; //$NON-NLS-1$

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            try {
                context.get(PHONE_NUMBER_HANDLER_KEY, p -> new PhoneNumberHandlerBase());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                context.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String possiblePhoneValue = row.get(columnId);
        if (StringUtils.isEmpty(possiblePhoneValue)) {
            return;
        }

        final String regionCode = getRegionCode(context, row);

        final String formatedStr = formatIfValid(regionCode,
                context.getParameters().get(FORMAT_TYPE_PARAMETER), possiblePhoneValue);
        row.set(columnId, formatedStr);
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
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(SelectParameter.Builder.builder() //
                .name(OtherColumnParameters.MODE_PARAMETER) //
                .item(OTHER_COLUMN_MODE, OTHER_COLUMN_MODE, //
                        new Parameter(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, //
                                ParameterType.COLUMN, //
                                StringUtils.EMPTY, false, false, StringUtils.EMPTY)) //
                .item(CONSTANT_MODE, CONSTANT_MODE, //
                        SelectParameter.Builder.builder().name(REGIONS_PARAMETER_CONSTANT_MODE).canBeBlank(true) //
                                .item(US_REGION_CODE, US_REGION_CODE) //
                                .item(FR_REGION_CODE, FR_REGION_CODE) //
                                .item(UK_REGION_CODE, UK_REGION_CODE) //
                                .item(DE_REGION_CODE, DE_REGION_CODE) //
                                .item(OTHER_REGION_TO_BE_SPECIFIED,
                                        new Parameter(MANUAL_REGION_PARAMETER_STRING, ParameterType.STRING, EMPTY))
                                .defaultValue(US_REGION_CODE).build()) //
                .defaultValue(CONSTANT_MODE).build());

        parameters.add(SelectParameter.Builder.builder().name(FORMAT_TYPE_PARAMETER) //
                .item(TYPE_INTERNATIONAL) //
                .item(TYPE_NATIONAL) //
                .item(TYPE_E164) //
                .item(TYPE_RFC3966) //
                .defaultValue(TYPE_INTERNATIONAL).build());
        return ActionsBundle.attachToAction(parameters, this);
    }

    private String getRegionCode(ActionContext context, DataSetRow row) {
        final Map<String, String> parameters = context.getParameters();
        final String regionParam;
        switch (parameters.get(OtherColumnParameters.MODE_PARAMETER)) {
        case CONSTANT_MODE:
            if (StringUtils.equals(OTHER_REGION_TO_BE_SPECIFIED, parameters.get(REGIONS_PARAMETER_CONSTANT_MODE))) {
                regionParam = parameters.get(MANUAL_REGION_PARAMETER_STRING);
            } else {
                regionParam = parameters.get(REGIONS_PARAMETER_CONSTANT_MODE);
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
    public String getCategory() {
        return ActionCategory.PHONE_NUMBER.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType())) || Type.INTEGER.equals(Type.get(column.getType()));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
