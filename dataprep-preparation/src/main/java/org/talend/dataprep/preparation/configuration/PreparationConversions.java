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

package org.talend.dataprep.preparation.configuration;

import static java.util.stream.Collectors.toList;
import static org.talend.dataprep.conversions.BeanConversionService.RegistrationBuilder.fromBean;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.preparation.*;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.preparation.service.UserPreparation;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

/**
 * A configuration for {@link Preparation} conversions. It adds all transient information (e.g. owner, action metadata...)
 */
@Configuration
public class PreparationConversions {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationConversions.class);

    /**
     * <h1>{@link BeanPostProcessor} notice</h1>
     * Don't use any {@link org.springframework.beans.factory.annotation.Autowired} in the
     * configuration as it will prevent autowired beans to be processed by BeanPostProcessor.
     */
    @Component
    public class Conversions implements BeanPostProcessor, ApplicationContextAware {

        private ApplicationContext applicationContext;

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            if (bean instanceof BeanConversionService) {
                final BeanConversionService conversionService = (BeanConversionService) bean;
                conversionService //
                        .register(fromBean(Preparation.class) //
                        .toBeans(PreparationMessage.class, UserPreparation.class, PersistentPreparation.class) //
                                .using(PreparationMessage.class, this::toPreparationMessage) //
                                .using(PreparationSummary.class, this::toStudioPreparation) //
                                .using(UserPreparation.class, (source, target) -> toUserPreparation(target)) //
                        .build()
                );
                return conversionService;
            }
            return bean;
        }

        private PreparationSummary toStudioPreparation(Preparation source, PreparationSummary target) {
            if (target.getOwner() == null) {
                final Security security = applicationContext.getBean(Security.class);
                Owner owner = new Owner(security.getUserId(), security.getUserDisplayName(), StringUtils.EMPTY);
                target.setOwner(owner);
            }

            final PreparationRepository preparationRepository = applicationContext.getBean(PreparationRepository.class);
            final ActionRegistry actionRegistry = applicationContext.getBean(ActionRegistry.class);

            // Allow distributed run
            // Get preparation actions
            PreparationActions prepActions = preparationRepository.get(source.getHeadId(), PreparationActions.class);
            if (prepActions != null) {
                List<Action> actions = prepActions.getActions();
                boolean allowDistributedRun = true;
                for (Action action : actions) {
                    final ActionDefinition actionDefinition = actionRegistry.get(action.getName());
                    if (actionDefinition.getBehavior().contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED)) {
                        allowDistributedRun = false;
                        break;
                    }
                }
                target.setAllowDistributedRun(allowDistributedRun);
            }

            return target;
        }

        private UserPreparation toUserPreparation(UserPreparation target) {
            if (target.getOwner() == null) {
                final Security security = applicationContext.getBean(Security.class);
                Owner owner = new Owner(security.getUserId(), security.getUserDisplayName(), StringUtils.EMPTY);
                target.setOwner(owner);
            }
            return target;
        }

        private PreparationMessage toPreparationMessage(Preparation source, PreparationMessage target) {
            final PreparationUtils preparationUtils = applicationContext.getBean(PreparationUtils.class);
            final PreparationRepository preparationRepository = applicationContext.getBean(PreparationRepository.class);
            final ActionRegistry actionRegistry = applicationContext.getBean(ActionRegistry.class);

            final List<Step> steps = preparationUtils.listSteps(source.getHeadId(), preparationRepository);
            target.setSteps(steps);

            // Steps diff metadata
            final List<StepDiff> diffs = steps.stream() //
                    .filter(step -> !Step.ROOT_STEP.id().equals(step.id())) //
                    .map(Step::getDiff) //
                    .collect(toList());
            target.setDiff(diffs);

            // Actions
            final Step head = preparationRepository.get(source.getHeadId(), Step.class);
            if (head != null && head.getContent() != null) {
                // Get preparation actions
                PreparationActions prepActions = preparationRepository.get(head.getContent().id(), PreparationActions.class);
                target.setActions(prepActions.getActions());
                List<Action> actions = prepActions.getActions();

                // Allow distributed run
                boolean allowDistributedRun = true;
                for (Action action : actions) {
                    final ActionDefinition actionDefinition = actionRegistry.get(action.getName());
                    if (actionDefinition.getBehavior().contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED)) {
                        allowDistributedRun = false;
                        break;
                    }
                }
                target.setAllowDistributedRun(allowDistributedRun);

                // Actions metadata
                if (actionRegistry == null) {
                    LOGGER.debug("No action metadata available, unable to serialize action metadata for preparation {}.",
                            source.id());
                } else {
                    List<ActionDefinition> actionDefinitions = actions.stream() //
                            .map(a -> actionRegistry.get(a.getName())) //
                            .collect(Collectors.toList());
                    target.setMetadata(actionDefinitions);
                }
            } else {
                target.setActions(Collections.emptyList());
                target.setSteps(Collections.singletonList(Step.ROOT_STEP));
                target.setMetadata(Collections.emptyList());
            }
            return target;
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
