//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.dataset.service.analysis.synchronous;

import org.talend.dataprep.dataset.service.analysis.DataSetAnalyzer;

/**
 * Synchronous analyzers are performed synchronously while the dataset creation. Hence their execution time must be as
 * fast as possible.
 */
public interface SynchronousDataSetAnalyzer extends DataSetAnalyzer {

    /**
     * @return A arbitrary number to order synchronous analysis (one synchronous analysis may depend on other
     * implementation results).
     */
    int order();
}
