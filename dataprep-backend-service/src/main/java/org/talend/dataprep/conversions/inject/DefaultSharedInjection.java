package org.talend.dataprep.conversions.inject;

import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.share.SharedResource;
import org.talend.dataprep.preparation.store.PersistentPreparation;

/**
 * A default implementation of {@link OwnerInjection} dedicated to environments with no security enabled.
 */
public class DefaultSharedInjection implements SharedInjection {

    @Override
    public PreparationDTO apply(PersistentPreparation persistentPreparation, PreparationDTO dto) {
        //do nothing
        return dto;
    }
}
