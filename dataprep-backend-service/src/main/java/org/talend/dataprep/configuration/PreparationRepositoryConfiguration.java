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

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
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
                            if (steps != null) {
                                final List<String> stepIds = steps.stream() //
                                        .map(Step::getId) //
                                        .collect(Collectors.toList());
                                persistentPreparation.setSteps(stepIds);
                            }
                            return persistentPreparation;
                        }) //
                        .build());
                // PersistentPreparation -> Preparation
                conversionService.register(fromBean(PersistentPreparation.class) //
                        .toBeans(Preparation.class) //
                        .using(Preparation.class, (persistentPreparation, preparation) -> {
                            final PreparationRepository repository = getPreparationRepository();
                            final List<String> persistentPreparationSteps = persistentPreparation.getSteps();
                            if (persistentPreparationSteps != null) {
                                final List<Step> steps = persistentPreparationSteps.stream() //
                                        .map(step -> conversionService.convert(repository.get(step, PersistentStep.class),
                                                Step.class)) //
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
                            if (step.getParent() != null) {
                                persistentStep.setParent(step.getParent().getId());
                            } else {
                                persistentStep.setParent(Step.ROOT_STEP.getId());
                            }
                            persistentStep.setContent(step.getContent().getId());
                            return persistentStep;
                        }) //
                        .build());
                // PersistentStep -> Step
                conversionService.register(fromBean(PersistentStep.class) //
                        .toBeans(Step.class) //
                        .using(Step.class, (persistentStep, step) -> {
                            final PreparationRepository repository = getPreparationRepository();
                            if (!Step.ROOT_STEP.getId().equals(persistentStep.getId())) {
                                step.setParent(conversionService
                                        .convert(repository.get(persistentStep.getParent(), PersistentStep.class), Step.class));
                            }
                            final PreparationActions content = repository.get(persistentStep.getContent(),
                                    PreparationActions.class);
                            step.setContent(content);
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
