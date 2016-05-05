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
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter.Builder;
import org.talend.dataquality.standardization.phone.PhoneNumberHandlerBase;
import static org.talend.dataprep.transformation.api.action.parameters.ParameterType.STRING;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

/**
 * format a validated phone number to a specified form.
 */
@Component(FormatPhoneNumber.ACTION_BEAN_PREFIX + FormatPhoneNumber.ACTION_NAME)
public class FormatPhoneNumber extends ActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
	public static final String ACTION_NAME = "format_phone_number"; //$NON-NLS-1$

	/** a region code parameter */
	protected static final String REGIONS_PARAMETER = "region_code"; //$NON-NLS-1$

	private static final String PHONE_NUMBER_HANDLER_KEY = "phone_number_handler_helper";//$NON-NLS-1$

	/** a manually input parameter of region code */
	protected static final String MANUAL_REGION_PARAMETER_STRING = "manual_region_string"; //$NON-NLS-1$

	private static final String SELECTED_REGION_KEY = "selected_region";//$NON-NLS-1$

	/** user selected the format type*/
	private static final String SELECTED_FORMAT_TYPE_KEY = "selected_format_type";//$NON-NLS-1$

	/** a parameter of format type */
	protected static final String FORMAT_TYPE_PARAMETER = "format_type"; //$NON-NLS-1$

	/** the follow 4 types is provided to user selection on UI */
	private final String TYPE_INTERNATIONAL = "International"; //$NON-NLS-1$

	private final String TYPE_E164 = "E164"; //$NON-NLS-1$

	private final String TYPE_NATIONAL = "National"; //$NON-NLS-1$
	
	private final String TYPE_RFC396 = "RFC396"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(FormatPhoneNumber.class);

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            try {
                context.get(PHONE_NUMBER_HANDLER_KEY, p -> new PhoneNumberHandlerBase());
                context.get(SELECTED_REGION_KEY, r ->getRegionCode(context));
                context.get(SELECTED_FORMAT_TYPE_KEY, r ->getFormatType(context));
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

        String formatedStr = formatIfValid(context,possiblePhoneValue);
        if (!StringUtils.isEmpty(formatedStr)) {
            row.set(columnId, formatedStr);
        }
    }

	/**
	 *  when the phone is a valid phone number,format it as the specified form.
	 * @param phone
	 * @param regionParam
	 * @param formatType
	 * @param phoneNumberHandler
	 * @return
	 */
	private String formatIfValid(ActionContext context, String phone) {
		String regionParam = context.get(SELECTED_REGION_KEY);
		PhoneNumberHandlerBase phoneNumberHandler = context.get(PHONE_NUMBER_HANDLER_KEY);
		if (!phoneNumberHandler.isValidPhoneNumber(phone, regionParam)) {
			return null;
		}
		String formatType = context.get(SELECTED_FORMAT_TYPE_KEY,r -> getFormatType(context));
		switch (formatType) {
		case TYPE_INTERNATIONAL:
			return phoneNumberHandler.formatInternational(phone, regionParam);
		case TYPE_E164:
			return phoneNumberHandler.formatE164(phone, regionParam);
		case TYPE_NATIONAL:
			return phoneNumberHandler.formatNational(phone, regionParam);
		case TYPE_RFC396:
			return phoneNumberHandler.formatRFC396(phone, regionParam);
		default:
			//nothing to do  here
		}

		return null;
	}
    
   

    @Override
    @Nonnull
	public List<Parameter> getParameters() {
		final List<Parameter> parameters = super.getParameters();
		final Set<String> supportedRegions = PhoneNumberUtil.getInstance()
				.getSupportedRegions();

		Builder regionSelectionParam = SelectParameter.Builder.builder()
				.name(REGIONS_PARAMETER).canBeBlank(true);
		supportedRegions.forEach(region -> regionSelectionParam.item(region));
		parameters.add(SelectParameter.Builder.builder()
						.name(REGIONS_PARAMETER)
						.canBeBlank(true)
						.item("US") //$NON-NLS-1$
						.item("FR") //$NON-NLS-1$
						.item("UK") //$NON-NLS-1$
						.item("DE") //$NON-NLS-1$
						.item("other (region)", new Parameter(MANUAL_REGION_PARAMETER_STRING, STRING, EMPTY)) //$NON-NLS-1$
						.defaultValue("US") //$NON-NLS-1$
						.build());
		parameters.add(SelectParameter.Builder.builder()
				.name(FORMAT_TYPE_PARAMETER)
				.item(TYPE_INTERNATIONAL) //$NON-NLS-1$
				.item(TYPE_E164) //$NON-NLS-1$
				.item(TYPE_NATIONAL) //$NON-NLS-1$
				.item(TYPE_RFC396) //$NON-NLS-1$
				.defaultValue(TYPE_INTERNATIONAL) //$NON-NLS-1$
				.build());
		return parameters;
	}
    
    private String getRegionCode(ActionContext context){
    	final Map<String, String> parameters = context.getParameters();
    	String regionParam = parameters.get(REGIONS_PARAMETER);
        if(StringUtils.isEmpty(regionParam)){
        	return Locale.getDefault().getCountry();
        }
        
        if (StringUtils.equals("other (region)", regionParam)) {
            return parameters.get(MANUAL_REGION_PARAMETER_STRING);
        }
        return regionParam;
    }
    
    private String getFormatType(ActionContext context){
    	final Map<String, String> parameters = context.getParameters();
    	String formatTypeParamm = parameters.get(FORMAT_TYPE_PARAMETER);
    	if(StringUtils.isEmpty(formatTypeParamm)){
    		return "International";
    	}
    	return formatTypeParamm;
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
