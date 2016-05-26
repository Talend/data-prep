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
package org.talend.dataprep.transformation.api.action.metadata.phonenumber;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.elasticsearch.common.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;
import org.talend.dataquality.standardization.phone.PhoneNumberHandlerBase;


/**
 * Format a validated phone number to a specified format.
 */
@Component(FormatPhoneNumber.ACTION_BEAN_PREFIX + FormatPhoneNumber.ACTION_NAME)
public class FormatPhoneNumber extends ActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
    public static final String ACTION_NAME = "format_phone_number"; //$NON-NLS-1$

    /** a region code parameter */
    protected static final String REGIONS_PARAMETER_CONSTANT_MODE = "region_code"; //$NON-NLS-1$

    private static final String PHONE_NUMBER_HANDLER_KEY = "phone_number_handler_helper";//$NON-NLS-1$

    /** a manually input parameter of region code */
    protected static final String MANUAL_REGION_PARAMETER_STRING = "manual_region_string"; //$NON-NLS-1$

    private static final String US_REGION_CODE = "US";

    private static final String FR_REGION_CODE = "FR";

    private static final String UK_REGION_CODE = "UK";

    private static final String DE_REGION_CODE = "DE";

    private static final String OTHER_REGION_TO_BE_SPECIFIED = "other (region)";

    /** a parameter of format type */
    protected static final String FORMAT_TYPE_PARAMETER = "format_type"; //$NON-NLS-1$

    /** the follow 4 types is provided to user selection on UI */
    private final String TYPE_INTERNATIONAL = "International"; //$NON-NLS-1$

    private final String TYPE_NATIONAL = "National"; //$NON-NLS-1$

    private final String TYPE_E164 = "E164"; //$NON-NLS-1$

    private final String TYPE_RFC396 = "RFC3966"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(FormatPhoneNumber.class);

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

        String regionCode = getRegionCode(context, row);

        String formatedStr = formatIfValid(regionCode, context.get(PHONE_NUMBER_HANDLER_KEY),
                context.getParameters().get(FORMAT_TYPE_PARAMETER), possiblePhoneValue);
        row.set(columnId, formatedStr);
    }

    /**
     * When the phone is a valid phone number,format it as the specified form.
     * 
     * @return the formatted phone number or the original value if cannot be formatted
     */
    private String formatIfValid(String regionParam, PhoneNumberHandlerBase phoneNumberHandler, String formatType, String phone) {
        if (!phoneNumberHandler.isValidPhoneNumber(phone, regionParam)) {
            return phone;
        }
        switch (formatType) {
        case TYPE_INTERNATIONAL:
            return phoneNumberHandler.formatInternational(phone, regionParam);
        case TYPE_NATIONAL:
            return phoneNumberHandler.formatNational(phone, regionParam);
        case TYPE_E164:
            return phoneNumberHandler.formatE164(phone, regionParam);
        case TYPE_RFC396:
            return phoneNumberHandler.formatRFC396(phone, regionParam);
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
                .item(OtherColumnParameters.OTHER_COLUMN_MODE, //
                        new Parameter(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, //
                                ParameterType.COLUMN, //
                                StringUtils.EMPTY, false, false, StringUtils.EMPTY, getMessagesBundle())) //
                .item(OtherColumnParameters.CONSTANT_MODE, //
                		SelectParameter.Builder.builder().name(REGIONS_PARAMETER_CONSTANT_MODE).canBeBlank(true) //
                        .item(US_REGION_CODE) //
                        .item(FR_REGION_CODE) //
                        .item(UK_REGION_CODE) //
                        .item(DE_REGION_CODE) //
                        .item(OTHER_REGION_TO_BE_SPECIFIED,
                                new Parameter(MANUAL_REGION_PARAMETER_STRING, ParameterType.STRING, EMPTY))
                        .defaultValue(US_REGION_CODE).build()) //
                
                .defaultValue(OtherColumnParameters.OTHER_COLUMN_MODE).build());

        parameters.add(SelectParameter.Builder.builder().name(FORMAT_TYPE_PARAMETER) //
                .item(TYPE_INTERNATIONAL) //
                .item(TYPE_NATIONAL) //
                .item(TYPE_E164) //
                .item(TYPE_RFC396) //
                .defaultValue(TYPE_INTERNATIONAL).build());
        return parameters;
    }

	private String getRegionCode(ActionContext context, DataSetRow row) {
		final Map<String, String> parameters = context.getParameters();
		String regionParam = null;
		if (OtherColumnParameters.CONSTANT_MODE.equals(parameters.get(OtherColumnParameters.MODE_PARAMETER))) {
			regionParam = parameters.get(REGIONS_PARAMETER_CONSTANT_MODE);
			if (StringUtils.equals(OTHER_REGION_TO_BE_SPECIFIED, regionParam)) {
				regionParam = parameters.get(MANUAL_REGION_PARAMETER_STRING);
			}
		} else if(OtherColumnParameters.SELECTED_COLUMN_PARAMETER.equals(parameters.get(OtherColumnParameters.MODE_PARAMETER))){
			final ColumnMetadata selectedColumn = context.getRowMetadata().getById(parameters
									.get(OtherColumnParameters.SELECTED_COLUMN_PARAMETER));
			regionParam = row.get(selectedColumn.getId());
		}
		if (StringUtils.isEmpty(regionParam)) {
			return Locale.getDefault().getCountry();
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
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType())) || Type.INTEGER.equals(Type.get(column.getType()));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
