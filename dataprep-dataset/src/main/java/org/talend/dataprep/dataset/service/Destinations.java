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

package org.talend.dataprep.dataset.service;

import java.io.InputStream;

public class Destinations {

    /**
     * JMS Destination for data statistics information of a data set.
     *
     * @see org.talend.dataprep.dataset.service.DataSetService#create(String, String, InputStream, String)
     */
    public static final String STATISTICS_ANALYSIS = "org.talend.tdp.dataset.content.statistics"; //$NON-NLS-1

    /**
     * Private constructor.
     */
    private Destinations() {
        // utility class should not have a public constructor.
    }
}
