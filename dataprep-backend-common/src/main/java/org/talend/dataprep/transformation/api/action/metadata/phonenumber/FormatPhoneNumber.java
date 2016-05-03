//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================
package org.talend.dataprep.transformation.api.action.metadata.phonenumber;


import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import org.elasticsearch.common.lang3.StringUtils;
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

/**
 * 
 * format a validated phone number to a specified form.
 *
 */
@Component(FormatPhoneNumber.ACTION_BEAN_PREFIX + FormatPhoneNumber.ACTION_NAME)
public class FormatPhoneNumber extends ActionMetadata implements ColumnAction {

	/**
	 * Action name.
	 */
	public static final String ACTION_NAME = "Format phone number"; //$NON-NLS-1$

	private String regionCode = Locale.getDefault().getCountry();

	protected static final String REGIONS_PARAMETER = "region"; //$NON-NLS-1$

	@Override
	public void applyOnColumn(DataSetRow row, ActionContext context) {
		final String columnId = context.getColumnId();
		final String possiblePhoneValue = row.get(columnId);
		if (StringUtils.isEmpty(possiblePhoneValue)) {
			return;
		}

		Map<String, String> parameters = context.getParameters();
		String regionParam = parameters.get(REGIONS_PARAMETER);
		if (!StringUtils.isEmpty(regionParam)) {
			regionCode = regionParam;
		}
		PhoneNumberHandlerBase phoneNumberHanler = new PhoneNumberHandlerBase();
		if (phoneNumberHanler
				.isValidPhoneNumber(possiblePhoneValue, regionCode)) {
			String formatInternational = phoneNumberHanler.formatInternational(
					possiblePhoneValue, regionCode);
			if (formatInternational != null) {
				row.set(columnId, formatInternational);
			}
		}

	}

	@Override
	@Nonnull
	public List<Parameter> getParameters() {
		final List<Parameter> parameters = super.getParameters();
		// @formatter:off
		Builder canBeBlank = SelectParameter.Builder.builder()
				.name(REGIONS_PARAMETER).canBeBlank(true);
		for (String code : Locale.getISOCountries()) {
			canBeBlank.item(code);
		}
		parameters.add(canBeBlank.defaultValue("US").build());
		// @formatter:on
		return parameters;
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

}
