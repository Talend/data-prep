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

import java.util.Locale;

import org.elasticsearch.common.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
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
	
	private String regionCode=Locale.getDefault().getCountry();
	


	@Override
	public void applyOnColumn(DataSetRow row, ActionContext context) {
		final String columnId = context.getColumnId();
		final String possiblePhoneValue = row.get(columnId);
		if (StringUtils.isEmpty(possiblePhoneValue)) {
			return;
		}
		final ColumnMetadata column = row.getRowMetadata().getById(columnId);
		String domain = column.getDomain();
		if (domain != null
				&& (domain.equals(SemanticCategoryEnum.DE_PHONE
						.getDisplayName())
						|| domain.equals(SemanticCategoryEnum.FR_PHONE
								.getDisplayName())
						|| domain.equals(SemanticCategoryEnum.US_PHONE
								.getDisplayName()) || domain
							.equals(SemanticCategoryEnum.UK_PHONE
									.getDisplayName()))) {
			regionCode = domain.split(" ")[0];
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
	public String getName() {
		return ACTION_NAME;
	}

	@Override
	public String getCategory() {
		return ActionCategory.PHONE_NUMBER.getDisplayName();
	}

	@Override
	public boolean acceptColumn(ColumnMetadata column) {
		boolean validType = Type.STRING.equals(Type.get(column.getType()))
				|| Type.INTEGER.equals(Type.get(column.getType()));
		if (!validType) {
			return false;
		}

		return true;
	}

}
