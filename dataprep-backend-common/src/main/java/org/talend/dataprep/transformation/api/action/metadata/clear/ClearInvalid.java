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

package org.talend.dataprep.transformation.api.action.metadata.clear;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.transformation.api.action.metadata.category.ActionScope.INVALID;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadataUtils;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Clear cell when value is invalid.
 */
@Component(ClearInvalid.ACTION_BEAN_PREFIX + ClearInvalid.ACTION_NAME)
public class ClearInvalid extends AbstractClear implements ColumnAction {

    /** the action name. */
    public static final String ACTION_NAME = "clear_invalid"; //$NON-NLS-1$

    private static final List<String> ACTION_SCOPE = Collections.singletonList(INVALID.getDisplayName());

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    /**
     * @see ActionMetadata#getActionScope()
     */
    @Override
    public List<String> getActionScope() {
        return ACTION_SCOPE;
    }

    public boolean toClear(ColumnMetadata colMetadata, String value, ActionContext context) {
        // update invalid values of column metadata to prevent unnecessary future analysis
        if (ActionMetadataUtils.checkInvalidValue(colMetadata, value)) {
            return true;
        }
        // valid value
        return false;
    }

}
