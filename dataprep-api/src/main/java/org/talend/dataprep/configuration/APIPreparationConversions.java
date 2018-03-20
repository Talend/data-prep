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

import static org.talend.dataprep.conversions.BeanConversionService.fromBean;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.api.EnrichedPreparation;
import org.talend.dataprep.api.service.command.preparation.LocatePreparation;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;
import org.talend.dataprep.security.SecurityProxy;

@Component
public class APIPreparationConversions extends BeanConversionServiceWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(APIPreparationConversions.class);

    @Override
    public BeanConversionService doWith(BeanConversionService conversionService, String beanName,
            ApplicationContext applicationContext) {
        conversionService.register(fromBean(PreparationMessage.class) //
                .toBeans(EnrichedPreparation.class) //
                .using(EnrichedPreparation.class,
                        (preparationMessage, enrichedPreparation) -> toEnrichedPreparation(preparationMessage,
                                enrichedPreparation, applicationContext)) //
                .build() //
        );
        return conversionService;
    }

    private EnrichedPreparation toEnrichedPreparation(PreparationMessage preparationMessage,
            EnrichedPreparation enrichedPreparation, ApplicationContext applicationContext) {
        final SecurityProxy securityProxy = applicationContext.getBean(SecurityProxy.class);
        // Add related dataset information
        if (preparationMessage.getDataSetId() == null) {
            return enrichedPreparation;
        } else {
            // get the dataset metadata
            try {
                securityProxy.asTechnicalUser(); // because dataset are not shared
                final DataSetGetMetadata bean = applicationContext.getBean(DataSetGetMetadata.class,
                        preparationMessage.getDataSetId());
                final DataSetMetadata dataSetMetadata = bean.execute();
                enrichedPreparation.setSummary(new EnrichedPreparation.DataSetMetadataSummary(dataSetMetadata));
            } catch (Exception e) {
                LOGGER.debug("error reading dataset metadata {} : {}", enrichedPreparation.getId(), e);
                return enrichedPreparation;
            } finally {
                securityProxy.releaseIdentity();
            }
        }

        // Add step ids
        LinkedList<String> collected = new LinkedList<>();
        preparationMessage.getSteps().stream().map(Step::getId).forEach(s -> {
            if (s != null && (collected.isEmpty() || !collected.getLast().equals(s))) {
                collected.add(s);
            }
        });
        enrichedPreparation.setSteps(collected);

        // Add folder information
        final LocatePreparation command = applicationContext.getBean(LocatePreparation.class, enrichedPreparation.getId());
        final Folder folder = command.execute();
        enrichedPreparation.setFolder(folder);

        return enrichedPreparation;
    }
}
