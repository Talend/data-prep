package org.talend.dataprep.preparation.configuration;

import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.preparation.store.PersistentPreparation;

import java.util.function.BiFunction;

/**
 * An API to ease {@link org.talend.dataprep.api.share.Owner} injection in {@link PreparationDTO} instances.
 *
 * @see org.talend.dataprep.conversions.BeanConversionService#convert(Object, Class, BiFunction[])
 */
public interface SharedInjection extends BiFunction<PersistentPreparation, PreparationDTO, PreparationDTO> {
}
