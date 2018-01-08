// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service.delegate;

import static org.talend.dataprep.command.CommandHelper.toStream;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.api.EnrichedPreparation;
import org.talend.dataprep.api.service.command.preparation.PreparationSearchByName;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.preparation.service.UserPreparation;

/**
 * A {@link SearchDelegate} implementation to search in preparations.
 */
@Component
public class PreparationSearchDelegate extends AbstractSearchDelegate<EnrichedPreparation> {

    @Autowired
    private BeanConversionService beanConversionService;

    @Override
    public String getSearchCategory() {
        return "preparations";
    }

    @Override
    public String getSearchLabel() {
        return "preparations";
    }

    @Override
    public String getInventoryType() {
        return "preparation";
    }

    @Override
    public Stream<EnrichedPreparation> search(String query, boolean strict) {
        final PreparationSearchByName command = getCommand(PreparationSearchByName.class, query, strict);
        return toStream(UserPreparation.class, mapper, command) //
                .map(userPreparation -> beanConversionService.convert(userPreparation, EnrichedPreparation.class));
    }

}
