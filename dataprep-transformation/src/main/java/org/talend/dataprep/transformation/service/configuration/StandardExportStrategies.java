// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.service.configuration;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.transformation.service.export.*;
import org.talend.dataprep.util.OrderedBeans;

@Configuration
public class StandardExportStrategies {

    @Bean
    OrderedBeans<StandardExportStrategy> exportStrategies(ApplyPreparationExportStrategy applyPreparationExportStrategy,
            DataSetExportStrategy dataSetExportStrategy, OptimizedExportStrategy optimizedExportStrategy,
            PreparationExportStrategy preparationExportStrategy) {
        // Order is important: it gives priority for one strategy over others.
        return new OrderedBeans<>(Arrays.asList(optimizedExportStrategy, //
                preparationExportStrategy, //
                dataSetExportStrategy, //
                applyPreparationExportStrategy));
    }

}
