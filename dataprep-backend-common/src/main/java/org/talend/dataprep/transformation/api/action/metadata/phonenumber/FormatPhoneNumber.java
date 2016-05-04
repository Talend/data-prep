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
import org.talend.dataprep.transformation.api.action.metadata.line.MakeLineHeader;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter.Builder;
import org.talend.dataquality.standardization.phone.PhoneNumberHandlerBase;

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

    private static final String defaultRegionCode = Locale.getDefault().getCountry();

    static final String REGIONS_PARAMETER = "region_code"; //$NON-NLS-1$

    private static final String PHONE_NUMBER_HANDLER_KEY = "phone_number_handler_helper";//$NON-NLS-1$

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

        Map<String, String> parameters = context.getParameters();
        String regionParam = parameters.get(REGIONS_PARAMETER);
        if (StringUtils.isEmpty(regionParam)) {
            // we should also test here if the selected region is valid
            // TODO this step should be done in the compile phase
            regionParam = defaultRegionCode;
        }
        PhoneNumberHandlerBase phoneNumberHandler = context.get(PHONE_NUMBER_HANDLER_KEY);
        if (phoneNumberHandler.isValidPhoneNumber(possiblePhoneValue, regionParam)) {
            String formatInternational = phoneNumberHandler.formatInternational(possiblePhoneValue, regionParam);
            if (formatInternational != null) {
                row.set(columnId, formatInternational);
            }
        }
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        final Set<String> supportedRegions = PhoneNumberUtil.getInstance().getSupportedRegions();

        Builder regionSelectionParam = SelectParameter.Builder.builder().name(REGIONS_PARAMETER).canBeBlank(true);
        supportedRegions.forEach(region -> regionSelectionParam.item(region));
        parameters.add(regionSelectionParam.defaultValue(defaultRegionCode).build());// $NON-NLS-1$

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

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
