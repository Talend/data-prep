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

package org.talend.dataprep.transformation.api.transformer;

import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;

/**
 * Base interface used to transform (apply preparations to) dataset content.
 */
public interface Transformer {

    /**
     * Build the executable transformer.
     *
     * @param input the dataset content.
     * @param configuration Configuration of the transformation (format format...).
     * @return the executable transformer.
     */
    ExecutableTransformer buildExecutable(DataSet input, Configuration configuration);

    /**
     * @param configuration Configuration of a transformation (format format...).
     * @return <code>true</code> if {@link Transformer transformer} implementation handles <code>configuration</code>,
     * <code>false</code> otherwise.
     */
    boolean accept(Configuration configuration);

}
