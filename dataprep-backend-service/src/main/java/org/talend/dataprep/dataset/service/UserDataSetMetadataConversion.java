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

package org.talend.dataprep.dataset.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.api.user.UserData;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.user.store.UserDataRepository;

import static org.talend.dataprep.conversions.BeanConversionService.fromBean;

/**
 * A configuration for {@link DataSetMetadata} conversions. It adds all transient information (e.g. favorite flags)
 */
@Component
public class UserDataSetMetadataConversion extends BeanConversionServiceWrapper {

    @Override
    public BeanConversionService doWith(BeanConversionService conversionService, String beanName,
            ApplicationContext applicationContext) {
        conversionService.register(fromBean(DataSetMetadata.class) //
                .toBeans(UserDataSetMetadata.class) //
                .using(UserDataSetMetadata.class, (dataSetMetadata, userDataSetMetadata) -> {
                    Security security = applicationContext.getBean(Security.class);
                    String userId = security.getUserId();

                    // update the dataset favorites
                    final UserData userData = applicationContext.getBean(UserDataRepository.class).get(userId);
                    if (userData != null) {
                        userDataSetMetadata
                                .setFavorite(userData.getFavoritesDatasets().contains(dataSetMetadata.getId()));
                    }

                    // and the owner (if not already present).
                    if (userDataSetMetadata.getOwner() == null) {
                        userDataSetMetadata
                                .setOwner(new Owner(userId, security.getUserDisplayName(), StringUtils.EMPTY));
                    }

                    return userDataSetMetadata;
                }) //
                .build());
        return conversionService;
    }

}
