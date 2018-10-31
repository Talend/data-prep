package org.talend.dataprep.preparation.configuration;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.conversions.inject.OwnerInjection;
import org.talend.dataprep.preparation.store.PersistentPreparation;

/**
 * A default implementation of {@link OwnerInjection} dedicated to environments with no security enabled.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class DefaultSharedInjection implements SharedInjection {

    @Override
    public PreparationDTO apply(PersistentPreparation persistentPreparation, PreparationDTO dto) {
        //do nothing
        return dto;
    }
}
