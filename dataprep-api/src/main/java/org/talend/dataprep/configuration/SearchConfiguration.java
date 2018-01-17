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

package org.talend.dataprep.configuration;

import static java.util.Arrays.asList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.api.service.delegate.DataSetSearchDelegate;
import org.talend.dataprep.api.service.delegate.FolderSearchDelegate;
import org.talend.dataprep.api.service.delegate.PreparationSearchDelegate;
import org.talend.dataprep.api.service.delegate.SearchDelegate;
import org.talend.dataprep.util.OrderedBeans;

/**
 * Defines search priorities for {@link org.talend.dataprep.api.service.SearchAPI}.
 */
@Configuration
public class SearchConfiguration {

    @Bean(name = "ordered#search")
    public OrderedBeans<SearchDelegate> searchDelegateOrderedBeans(FolderSearchDelegate folder, //
                                                            DataSetSearchDelegate dataset, //
                                                            PreparationSearchDelegate preparation) {
        return new OrderedBeans<>(asList(preparation, folder, dataset));
    }
}
