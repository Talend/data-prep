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

package org.talend.dataprep.configuration;

import static org.talend.dataprep.conversions.BeanConversionService.RegistrationBuilder.fromBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PersistentPreparationRepository;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;

/**
 * A configuration that performs the following:
 * <ul>
 * <li>Wrap the active {@link PreparationRepository} and wrap it using {@link PersistentPreparationRepository}.</li>
 * <li>Configure all conversions from {@link org.talend.dataprep.preparation.store.PersistentIdentifiable} to
 * {@link org.talend.dataprep.api.preparation.Identifiable} (back and forth).</li>
 * </ul>
 */
@Configuration
public class PreparationRepositoryConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationRepositoryConfiguration.class);

    /**
     * <h1>{@link BeanPostProcessor} notice</h1>
     * Don't use any {@link org.springframework.beans.factory.annotation.Autowired} in the
     * configuration as it will prevent autowired beans to be processed by BeanPostProcessor.
     */
    @Component
    public class PreparationRepositoryPostProcessor implements BeanPostProcessor, ApplicationContextAware {

        private ApplicationContext applicationContext;

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof PreparationRepository) {
                if ("preparationRepository#mongodb".equals(beanName)) {
                    LOGGER.info("Skip wrapping of '{}' (not a primary implementation).", beanName);
                    return bean;
                }
                LOGGER.info("Wrapping '{}' ({})...", bean.getClass(), beanName);
                final BeanConversionService beanConversionService = applicationContext.getBean(BeanConversionService.class);
                return new PersistentPreparationRepository((PreparationRepository) bean, beanConversionService);
            }
            return bean;
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }

    /**
     * <h1>{@link BeanPostProcessor} notice</h1>
     * Don't use any {@link org.springframework.beans.factory.annotation.Autowired} in the
     * configuration as it will prevent autowired beans to be processed by BeanPostProcessor.
     */
    @Component
    public class PersistentPreparationConversions implements BeanPostProcessor, ApplicationContextAware {

        private ApplicationContext applicationContext;

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (bean instanceof BeanConversionService) {
                final BeanConversionService conversionService = (BeanConversionService) bean;
                // Preparation -> PersistentPreparation
                conversionService.register(fromBean(Preparation.class) //
                        .toBeans(PersistentPreparation.class) //
                        .using(PersistentPreparation.class, (preparation, persistentPreparation) -> {
                            final List<Step> steps = preparation.getSteps();
                            final List<String> stepIds = steps.stream().map(Step::getId).collect(Collectors.toList());

                            if (stepIds.contains(preparation.getHeadId())) {
                                // Easy case: new head is part of previous steps, create a sub list of steps
                                final List<String> stepIdSubList = new ArrayList<>();
                                for (String currentStepId : stepIds) {
                                    stepIdSubList.add(currentStepId);
                                    if (currentStepId.equals(preparation.getHeadId())) {
                                        break;
                                    }
                                }
                                persistentPreparation.setSteps(stepIdSubList);
                            } else if (!stepIds.contains(preparation.getHeadId()) && StringUtils.equals(preparation.getHeadId(), stepIds.get(stepIds.size() - 1))) {
                                // Complete override of steps, set them as is (as long as it exists).
                                persistentPreparation.setSteps(stepIds);
                            } else {
                                /*
                                 * More complex: new head is *not* part of previous steps, delegate to PreparationUtils
                                 * Warning: doing so has some performance impacts (list may perform many getById calls
                                 * depending on preparation size)
                                 */
                                final PreparationUtils preparationUtils = applicationContext.getBean(PreparationUtils.class);
                                final PreparationRepository repository = applicationContext.getBean(PreparationRepository.class);

                                final List<String> storageSteps = preparationUtils.listStepsIds(preparation.getHeadId(),
                                        repository);
                                persistentPreparation.setSteps(storageSteps);

                            }
                            return persistentPreparation;
                        }) //
                        .build());
                // PersistentPreparation -> Preparation
                conversionService.register(fromBean(PersistentPreparation.class) //
                        .toBeans(Preparation.class) //
                        .using(Preparation.class, (persistentPreparation, preparation) -> {
                            final PreparationRepository repository = getPreparationRepository();
                            final List<String> preparationSteps = persistentPreparation.getSteps();

                            if (preparationSteps != null) {
                                if (preparationSteps.isEmpty()) {
                                    preparation.setSteps(Collections.singletonList(Step.ROOT_STEP));
                                } else {
                                    final String stepIds = preparationSteps.stream() //
                                            .map(id -> "'" + id + "'") //
                                            .collect(Collectors.joining(","));
                                    final Stream<PersistentStep> stream = repository
                                            .list(PersistentStep.class, "id in [" + stepIds + "]")
                                            .sorted(Comparator.comparingInt(o -> preparationSteps.indexOf(o.getId())));
                                    final List<Step> steps = stream.map(s -> conversionService.convert(s, Step.class)) //
                                            .collect(Collectors.toList());
                                    preparation.setSteps(steps);
                                }
                            } else {
                                final PreparationUtils preparationUtils = applicationContext.getBean(PreparationUtils.class);
                                final List<String> stepIds = preparationUtils.listStepsIds(preparation.getHeadId(), repository);

                                final List<Step> steps = stepIds.stream() //
                                        .map(stepId -> repository.get(stepId, Step.class)) //
                                        .collect(Collectors.toList());
                                preparation.setSteps(steps);
                            }

                            return preparation;
                        }) //
                        .build());
                // Step -> PersistentStep
                conversionService.register(fromBean(Step.class) //
                        .toBeans(PersistentStep.class) //
                        .using(PersistentStep.class, (step, persistentStep) -> {
                            persistentStep.setParentId(step.getParent());
                            return persistentStep;
                        }) //
                        .build());
                // PersistentStep -> Step
                conversionService.register(fromBean(PersistentStep.class) //
                        .toBeans(Step.class) //
                        .using(Step.class, (persistentStep, step) -> {
                            step.setParent(persistentStep.getParentId());
                            return step;
                        }) //
                        .build());
                return conversionService;
            }
            return bean;
        }

        private PreparationRepository getPreparationRepository() {
            return applicationContext.getBean(PreparationRepository.class);
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }

}
