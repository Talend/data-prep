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

package org.talend.dataprep.api.preparation;

/**
 * Bean that extends Preparation used for json serialization towards frontend.
 * 
 * @see org.talend.dataprep.api.preparation.json.PreparationDetailsJsonSerializer
 */
public class PreparationDetails {

    /** The wrapped Preparation. */
    private Preparation preparation;

    /**
     * Default constructor.
     *
     * @param source the source preparation.
     */
    public PreparationDetails(Preparation source) {
        this.preparation = source;
    }

    /**
     * @return the Preparation
     */
    public Preparation getPreparation() {
        return preparation;
    }
}
