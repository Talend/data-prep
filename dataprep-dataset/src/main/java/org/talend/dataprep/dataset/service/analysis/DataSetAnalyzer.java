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

package org.talend.dataprep.dataset.service.analysis;

import java.util.List;

import org.talend.dataprep.dataset.service.analysis.synchronous.SynchronousDataSetAnalyzer;

/**
 * Represents a component to analyze data set content. Analyzers are split into 2 categories:
 * <ul>
 * <li>Synchronous: see {@link SynchronousDataSetAnalyzer}</li>
 * </ul>
 * Synchronous analyzers are called during data set creation and executed sequentially. Asynchronous can execute
 * concurrently and should listen to a JMS queue.
 * 
 * @see SynchronousDataSetAnalyzer
 * @see org.talend.dataprep.dataset.service.DataSetService#analyzeDataSet(String, boolean, List)
 */
public interface DataSetAnalyzer {

    /**
     * Analyze data available in data set with <code>dataSetId</code> id.
     *
     * @param dataSetId A Data Set id. Implementations are responsible to check if data set still exists at the moment
     * it's called.
     */
    void analyze(String dataSetId);

}
