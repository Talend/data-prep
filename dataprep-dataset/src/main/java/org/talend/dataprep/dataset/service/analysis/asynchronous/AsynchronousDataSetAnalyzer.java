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

package org.talend.dataprep.dataset.service.analysis.asynchronous;

import org.talend.dataprep.dataset.service.analysis.DataSetAnalyzer;

public interface AsynchronousDataSetAnalyzer extends DataSetAnalyzer {

    /**
     * @return A String that contains a JMS queue name (the one this implementation is listening to).
     */
    String destination();
}
