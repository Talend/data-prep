// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.transformation.api.action.metadata;

import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.parameters.Item;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

/**
 * Base class for all single column action.
 */
public abstract class SingleColumnAction implements ActionMetadata {

    /** Name of the parameter for the column id. */
    public static final String COLUMN_ID = "column_id"; //$NON-NLS-1$

    /** The parameter object for the column id. */
    public static final Parameter COLUMN_ID_PARAMETER = new Parameter(COLUMN_ID, Type.STRING.getName(), StringUtils.EMPTY);

    /**
     * Name of the parameter for the column name. <strong>The name of the column is only for information usage, only
     * column id should be used.</strong> Storing the column name in the parameter is an easy way to display the name of
     * the column.
     */
    public static final String COLUMN_NAME = "column_name"; //$NON-NLS-1$

    /** The parameter object for the column name. */
    public static final Parameter COLUMN_NAME_PARAMETER = new Parameter(COLUMN_NAME, Type.STRING.getName(), StringUtils.EMPTY);

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { COLUMN_ID_PARAMETER, COLUMN_NAME_PARAMETER };
    }

    /**
     * By default, no Item.
     * 
     * @see ActionMetadata#getItems()
     */
    @Override
    @Nonnull
    public Item[] getItems() {
        return new Item[0];
    }

    /**
     * @param parameters the parameters.
     * @return the column id parameter.
     */
    protected String getColumnIdParameter(Map<String, String> parameters) {
        String columnId = parameters.get(COLUMN_ID);
        if (columnId == null) {
            throw new IllegalArgumentException("Parameter '" + COLUMN_ID + "' is required for this action");
        }
        return columnId;
    }

}
