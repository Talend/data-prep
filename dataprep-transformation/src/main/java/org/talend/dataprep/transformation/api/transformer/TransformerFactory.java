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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;

/**
 * Interface used by all TransformerFactories.
 */
@Component
public class TransformerFactory {

    @Autowired
    private ApplicationContext context;

    // Intentionally left private to prevent non-Spring managed initialization.
    private TransformerFactory() {
    }

    /**
     * Generate the wanted Transformer.
     * 
     * @return the Transformer.
     * @param configuration A {@link Configuration configuration} for a transformation.
     */
    public Transformer get(@Nonnull Configuration configuration) {
        List<Transformer> electedTransformers = context.getBeansOfType(Transformer.class).values().stream() //
                .filter(transformer -> transformer.accept(configuration)) //
                .collect(Collectors.toList());
        if (electedTransformers.isEmpty()) {
            throw new IllegalStateException("No transformers eligible for configuration.");
        }
        if (electedTransformers.size() > 1) {
            throw new IllegalStateException("Too many transformers eligible for configuration (got " + electedTransformers.size()
                    + ": " + Arrays.toString(electedTransformers.toArray()) + ")");
        }
        return electedTransformers.get(0);
    }

}
