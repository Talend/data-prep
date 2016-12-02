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

package org.talend.dataprep.dataset.configuration;

import static org.talend.dataprep.conversions.BeanConversionService.RegistrationBuilder.fromBean;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.api.user.UserData;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.service.UserDataSetMetadata;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.user.store.UserDataRepository;

/**
 * A configuration for {@link DataSetMetadata} conversions. It adds all transient information (e.g. favorite flags)
 */
@Configuration
public class DataSetConversions {

    /**
     * <h1>{@link BeanPostProcessor} notice</h1>
     * Don't use any {@link org.springframework.beans.factory.annotation.Autowired} in the
     * configuration as it will prevent autowired beans to be processed by BeanPostProcessor.
     */
    @Component
    private class DataSetMetadataConversions implements BeanPostProcessor, ApplicationContextAware {

        private ApplicationContext applicationContext;

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            if (bean instanceof BeanConversionService) {
                final BeanConversionService conversionService = (BeanConversionService) bean;
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

                            // and the owner
                            userDataSetMetadata.setOwner(new Owner(userId, security.getUserDisplayName(), StringUtils.EMPTY));

                            return userDataSetMetadata;
                        }) //
                        .build()
                );
                return conversionService;
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            return bean;
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }

}
