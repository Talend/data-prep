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
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.service.command.preparation.PreparationSearchByName;
import org.talend.dataprep.conversions.BeanConversionService;

/**
 * A {@link SearchDelegate} implementation to search in preparations.
 */
@Component
public class PreparationSearchDelegate extends AbstractSearchDelegate<PreparationDTO> {

    @Autowired
    private BeanConversionService beanConversionService;

    @Override
    public String getSearchCategory() {
        return "preparation";
    }

    @Override
    public String getSearchLabel() {
        return "preparation";
    }

    @Override
    public String getInventoryType() {
        return "preparation";
    }

    @Override
    public Stream<PreparationDTO> search(String query, boolean strict) {
        final PreparationSearchByName command = getCommand(PreparationSearchByName.class, query, strict);
        return toStream(PreparationDTO.class, mapper, command);
    }
}
