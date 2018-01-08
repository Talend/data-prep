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

package org.talend.dataprep.dataset.configuration;

import static org.talend.dataprep.conversions.BeanConversionService.fromBean;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.api.user.UserData;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.service.UserDataSetMetadata;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.user.store.UserDataRepository;

/**
 * A configuration for {@link DataSetMetadata} conversions. It adds all transient information (e.g. favorite flags)
 */
@Configuration
public class DataSetConversions extends BeanConversionServiceWrapper {

    @Override
    public BeanConversionService doWith(BeanConversionService conversionService, String beanName, ApplicationContext applicationContext) {
        conversionService.register(fromBean(DataSetMetadata.class) //
                .toBeans(UserDataSetMetadata.class) //
                .using(UserDataSetMetadata.class, (dataSetMetadata, userDataSetMetadata) -> {
                    final Security security = applicationContext.getBean(Security.class);
                    final UserDataRepository userDataRepository = applicationContext.getBean(UserDataRepository.class);
                    String userId = security.getUserId();

                    // update the dataset favorites
                    final UserData userData = userDataRepository.get(userId);
                    if (userData != null) {
                        userDataSetMetadata.setFavorite(userData.getFavoritesDatasets().contains(dataSetMetadata.getId()));
                    }

                    // and the owner (if not already present).
                    if (userDataSetMetadata.getOwner() == null) {
                        userDataSetMetadata.setOwner(new Owner(userId, security.getUserDisplayName(), StringUtils.EMPTY));
                    }

                    return userDataSetMetadata;
                }) //
                .build());
        return conversionService;
    }

}
