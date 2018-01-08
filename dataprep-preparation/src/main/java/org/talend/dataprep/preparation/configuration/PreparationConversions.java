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

package org.talend.dataprep.preparation.configuration;

import static java.util.stream.Collectors.toList;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.talend.dataprep.conversions.BeanConversionService.fromBean;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.CREATE_NEW_COLUMN;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.action.ActionForm;
import org.talend.dataprep.api.preparation.*;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.preparation.service.UserPreparation;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

/**
 * A configuration for {@link Preparation} conversions. It adds all transient information (e.g. owner, action
 * metadata...)
 */
@Component
public class PreparationConversions extends BeanConversionServiceWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationConversions.class);

    @Override
    public BeanConversionService doWith(BeanConversionService conversionService, String beanName,
                                        ApplicationContext applicationContext) {
        conversionService.register(fromBean(Preparation.class) //
                .toBeans(PreparationMessage.class, UserPreparation.class, PersistentPreparation.class) //
                .using(PreparationMessage.class, (s, t) -> toPreparationMessage(s, t, applicationContext)) //
                .using(PreparationSummary.class, (s, t) -> toStudioPreparation(s, t, applicationContext)) //
                .using(UserPreparation.class, (s, t) -> toUserPreparation(t, applicationContext)) //
                .build());
        return conversionService;
    }

    private PreparationSummary toStudioPreparation(Preparation source, PreparationSummary target,
                                                   ApplicationContext applicationContext) {
        if (target.getOwner() == null) {
            final Security security = applicationContext.getBean(Security.class);
            Owner owner = new Owner(security.getUserId(), security.getUserDisplayName(), StringUtils.EMPTY);
            target.setOwner(owner);
        }

        final PreparationRepository preparationRepository = applicationContext.getBean(PreparationRepository.class);
        final ActionRegistry actionRegistry = applicationContext.getBean(ActionRegistry.class);

        // Get preparation actions
        PreparationActions prepActions = preparationRepository.get(source.getHeadId(), PreparationActions.class);
        if (prepActions != null) {
            List<Action> actions = prepActions.getActions();
            boolean allowDistributedRun = true;
            for (Action action : actions) {
                final ActionDefinition actionDefinition = actionRegistry.get(action.getName());
                if (actionDefinition.getBehavior().contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED)) {
                    allowDistributedRun = false; // Disallow distributed run
                    break;
                }
            }
            target.setAllowDistributedRun(allowDistributedRun);
        }

        return target;
    }

    private UserPreparation toUserPreparation(UserPreparation target, ApplicationContext applicationContext) {
        if (target.getOwner() == null) {
            final Security security = applicationContext.getBean(Security.class);
            Owner owner = new Owner(security.getUserId(), security.getUserDisplayName(), StringUtils.EMPTY);
            target.setOwner(owner);
        }
        return target;
    }

    private PreparationMessage toPreparationMessage(Preparation source, PreparationMessage target, ApplicationContext applicationContext) {
        final PreparationRepository preparationRepository = applicationContext.getBean(PreparationRepository.class);
        final ActionRegistry actionRegistry = applicationContext.getBean(ActionRegistry.class);

        // Steps diff metadata
        final List<StepDiff> diffs = source.getSteps().stream() //
                .filter(step -> !Step.ROOT_STEP.id().equals(step.id())) //
                .map(Step::getDiff) //
                .collect(toList());
        target.setDiff(diffs);

        // Actions
        if (source.getHeadId() != null) {
            // Get preparation actions
            final String headId = source.getHeadId();
            final Step head = preparationRepository.get(headId, Step.class);
            if (head != null) {
                final PreparationActions prepActions = preparationRepository.get(head.getContent(), PreparationActions.class);
                if (prepActions != null) {
                    final List<Action> actions = prepActions.getActions();
                    target.setActions(prepActions.getActions());

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

                    // no need to have lock information (and may also break StandAlonePreparationParser...)
                    target.setLock(null);

                    // Actions metadata
                    if (actionRegistry == null) {
                        LOGGER.debug("No action metadata available, unable to serialize action metadata for preparation {}.",
                                source.id());
                    } else {
                        List<ActionForm> actionDefinitions = actions.stream() //
                                .map(a -> actionRegistry.get(a.getName())) //
                                .map(a -> a.getActionForm(getLocale())) //
                                .map(PreparationConversions::disallowColumnCreationChange) //
                                .collect(Collectors.toList());
                        target.setMetadata(actionDefinitions);
                    }
                }
            } else {
                LOGGER.warn("Head step #{} for preparation #{} does not exist.", headId, source.id());
                target.setActions(Collections.emptyList());
                target.setSteps(Collections.singletonList(Step.ROOT_STEP));
                target.setMetadata(Collections.emptyList());
            }
        } else {
            target.setActions(Collections.emptyList());
            target.setSteps(Collections.singletonList(Step.ROOT_STEP));
            target.setMetadata(Collections.emptyList());
        }
        return target;
    }

    /**
     * For a given action form, it will disallow edition on all column creation check. It is a safety specified in TDP-4531 to
     * avoid removing columns used by other actions.
     * <p>
     * Such method is not ideal as the system should be able to handle such removal in  a much more generic way.
     * </p>
     */
    private static ActionForm disallowColumnCreationChange(ActionForm form) {
        form.getParameters().stream().filter(p -> CREATE_NEW_COLUMN.equals(p.getName())).forEach(p -> p.setReadonly(true));
        return form;
    }
}
