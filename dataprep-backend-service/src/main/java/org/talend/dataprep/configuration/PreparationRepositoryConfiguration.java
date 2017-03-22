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
import org.springframework.context.ApplicationContext;
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
import org.talend.dataprep.processor.BeanConversionServiceWrapper;
import org.talend.dataprep.processor.Wrapper;

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

    @Component
    public class PreparationRepositoryPostProcessor implements Wrapper<PreparationRepository> {

        @Override
        public Class<PreparationRepository> wrapped() {
            return PreparationRepository.class;
        }

        @Override
        public PreparationRepository doWith(PreparationRepository instance, String beanName, ApplicationContext applicationContext) {
            if ("preparationRepository#mongodb".equals(beanName)) {
                LOGGER.info("Skip wrapping of '{}' (not a primary implementation).", beanName);
                return instance;
            }
            LOGGER.info("Wrapping '{}' ({})...", instance.getClass(), beanName);
            final BeanConversionService beanConversionService = applicationContext.getBean(BeanConversionService.class);
            return new PersistentPreparationRepository(instance, beanConversionService);
        }
    }

    @Component
    public class PersistentPreparationConversions extends BeanConversionServiceWrapper {

        @Override
        public BeanConversionService doWith(BeanConversionService conversionService, String beanName, ApplicationContext applicationContext) {
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
                        final PreparationRepository repository = getPreparationRepository(applicationContext);
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
                            persistentStep.setParentId(step.getParent().getId());
                        } else {
                            final String rootStepId = Step.ROOT_STEP.getId();
                            if (!rootStepId.equals(step.getId())) {
                                persistentStep.setParentId(rootStepId);
                            }
                        }
                        persistentStep.setContent(step.getContent().getId());
                        return persistentStep;
                    }) //
                    .build());
            // PersistentStep -> Step
            conversionService.register(fromBean(PersistentStep.class) //
                    .toBeans(Step.class) //
                    .using(Step.class, (persistentStep, step) -> {
                        final PreparationRepository repository = getPreparationRepository(applicationContext);
                        if (!Step.ROOT_STEP.getId().equals(persistentStep.getId())) {
                            step.setParent(conversionService
                                    .convert(repository.get(persistentStep.getParentId(), PersistentStep.class), Step.class));
                        }
                        final PreparationActions content = repository.get(persistentStep.getContent(),
                                PreparationActions.class);
                        step.setContent(content);
                        return step;
                    }) //
                    .build());
            return conversionService;
        }

        private PreparationRepository getPreparationRepository(ApplicationContext applicationContext) {
            return applicationContext.getBean(PreparationRepository.class);
        }
    }

}
