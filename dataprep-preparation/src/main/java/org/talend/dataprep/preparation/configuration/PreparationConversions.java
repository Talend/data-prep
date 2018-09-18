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
import static org.talend.tql.api.TqlBuilder.in;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.action.ActionForm;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.filter.FilterTranslator;
import org.talend.dataprep.api.filter.TQLFilterService;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.preparation.PreparationDetailsDTO;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.preparation.PreparationSummary;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.api.preparation.StepRowMetadata;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.preparation.service.PreparationService;
import org.talend.dataprep.preparation.service.UserPreparation;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

/**
 * A configuration for {@link Preparation} conversions. It adds all transient information (e.g. owner, action
 * metadata...)
 */
@Component
public class PreparationConversions extends BeanConversionServiceWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationConversions.class);

    private final FilterTranslator translator = new FilterTranslator();

    private final TQLFilterService tqlFilterService = new TQLFilterService();

    /**
     * For a given action form, it will disallow edition on all column creation check. It is a safety specified in
     * TDP-4531 to
     * avoid removing columns used by other actions.
     * <p>
     * Such method is not ideal as the system should be able to handle such removal in a much more generic way.
     * </p>
     */
    private static ActionForm disallowColumnCreationChange(ActionForm form) {
        form.getParameters().stream().filter(p -> CREATE_NEW_COLUMN.equals(p.getName())).forEach(
                p -> p.setReadonly(true));
        return form;
    }

    @Override
    public BeanConversionService doWith(BeanConversionService conversionService, String beanName,
            ApplicationContext applicationContext) {
        conversionService.register(fromBean(Preparation.class) //
                .toBeans(PreparationMessage.class, UserPreparation.class, PersistentPreparation.class,
                        PreparationDTO.class) //
                .using(PreparationMessage.class, (s, t) -> toPreparationMessage(s, t, applicationContext)) //
                .using(PreparationSummary.class, (s, t) -> toStudioPreparation(s, t, applicationContext)) //
                .build());

        // convert PreparationDTO to PreparationDetailsDTO
        conversionService.register(fromBean(PreparationDTO.class) //
                .toBeans(PreparationDetailsDTO.class) //
                .using(PreparationDetailsDTO.class, (s, t) -> toPreparationDetailsDTO(s, t, applicationContext)) //
                .build());
        return conversionService;
    }

    private PreparationDetailsDTO toPreparationDetailsDTO(PreparationDTO source, PreparationDetailsDTO target,
            ApplicationContext applicationContext) {

        final PreparationRepository preparationRepository = applicationContext.getBean(PreparationRepository.class);

        List<String> idsStep = source.getSteps();

        final List<StepDiff> diffs = preparationRepository
                .list(PersistentStep.class, in("id", idsStep.toArray(new String[] {})))
                .filter(step -> !Step.ROOT_STEP.id().equals(step.getId())) //
                .sorted((step1, step2) -> {
                    // we need to keep the order from the original list (source.getSteps())
                    int idPosStep1 = idsStep.indexOf(step1.getId());
                    int idPosStep2 = idsStep.indexOf(step2.getId());

                    return idPosStep1 - idPosStep2;
                })
                .map(PersistentStep::getDiff) //
                .collect(toList());
        target.setDiff(diffs);

        // TDP-5888: It is important for Spark runs to have a row metadata to describe initial data schema.
        // and also to display column names in filter labels of steps
        final PersistentPreparation preparation =
                preparationRepository.get(source.getId(), PersistentPreparation.class);
        target.setRowMetadata(preparation.getRowMetadata());

        injectColumnNamesIntoActions(source.getHeadId(), target, applicationContext.getBean(PreparationService.class));

        return target;
    }

    /**
     * Inject column names into actions to display correctly every action label and filter label.
     *
     * @param headId the id of the head step of the version
     * @param target the already converted object to enrich
     * @param preparationService the service to find actions to enrich and inject into the converted object
     */
    private void injectColumnNamesIntoActions(String headId, PreparationDetailsDTO target,
            PreparationService preparationService) {

        final List<Action> actions = preparationService.getVersionedAction(target.getId(), headId);
        target.setActions(actions);
        for (Action action : actions) {
            Map<String, String> parameters = action.getParameters();
            List<ColumnMetadata> filterColumns = new ArrayList<>();

            // Fetch column metadata relative to the filtered action
            // Ask for (n-1) metadata (necessary if some columns are deleted during last step)
            RowMetadata rowMetadata =
                    preparationService.getPreparationStep(target.getSteps().get(actions.indexOf(action)));
            if (rowMetadata == null) {
                rowMetadata = target.getRowMetadata();
            }
            if (StringUtils.isNotBlank(parameters.get(ImplicitParameters.FILTER.getKey()))) {
                // Translate filter from JSON to TQL
                parameters.put(ImplicitParameters.FILTER.getKey(),
                        translator.toTQL(parameters.get(ImplicitParameters.FILTER.getKey())));
                filterColumns = tqlFilterService
                        .getFilterColumnsMetadata(parameters.get(ImplicitParameters.FILTER.getKey()), rowMetadata);
            }
            // add metadata of the scope column if not already added (useful when there is a column rename for
            // example)
            if (filterColumns
                    .stream()
                    .filter(column -> column.getId().equals(parameters.get(ImplicitParameters.COLUMN_ID.getKey())))
                    .findFirst()
                    .orElse(null) == null) {
                filterColumns
                        .addAll(rowMetadata
                                .getColumns()
                                .stream()
                                .filter(column -> column
                                        .getId()
                                        .equals(parameters.get(ImplicitParameters.COLUMN_ID.getKey())))
                                .collect(Collectors.toList()));
            }
            action.setFilterColumns(filterColumns);
        }
    }

    private PreparationSummary toStudioPreparation(Preparation source, PreparationSummary target,
            ApplicationContext applicationContext) {
        final PreparationRepository preparationRepository = applicationContext.getBean(PreparationRepository.class);
        final ActionRegistry actionRegistry = applicationContext.getBean(ActionRegistry.class);

        // Get preparation actions
        PreparationActions prepActions = preparationRepository.get(source.getHeadId(), PreparationActions.class);
        if (prepActions != null) {
            List<Action> actions = prepActions.getActions();
            boolean allowDistributedRun = true;
            for (Action action : actions) {
                final ActionDefinition actionDefinition = actionRegistry.get(action.getName());
                if (actionDefinition.getBehavior(action).contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED)) {
                    allowDistributedRun = false; // Disallow distributed run
                    break;
                }
            }
            target.setAllowDistributedRun(allowDistributedRun);
        }

        return target;
    }

    // TODO: check if it is always used
    private PreparationMessage toPreparationMessage(Preparation source, PreparationMessage target,
            ApplicationContext applicationContext) {
        final PreparationRepository preparationRepository = applicationContext.getBean(PreparationRepository.class);
        final ActionRegistry actionRegistry = applicationContext.getBean(ActionRegistry.class);

        // Steps diff metadata
        final List<StepDiff> diffs = source
                .getSteps()
                .stream() //
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
                final PreparationActions prepActions =
                        preparationRepository.get(head.getContent(), PreparationActions.class);
                if (prepActions != null) {
                    final List<Action> actions = prepActions.getActions();

                    for (Action action : actions) {
                        Map<String, String> parameters = action.getParameters();
                        List<ColumnMetadata> filterColumns = new ArrayList<>();

                        // Fetch column metadata relative to the filtered action
                        // Ask for (n-1) metadata (necessary if some columns are deleted during last step)
                        final Step step = preparationRepository
                                .get(source.getSteps().get(actions.indexOf(action)).getId(), Step.class);
                        if (step != null) {
                            final StepRowMetadata stepRowMetadata =
                                    preparationRepository.get(step.getRowMetadata(), StepRowMetadata.class);

                            if (stepRowMetadata == null) {
                                filterColumns = target.getRowMetadata().getColumns();
                            } else {
                                if (StringUtils.isNotBlank(parameters.get(ImplicitParameters.FILTER.getKey()))) {
                                    // Translate filter from JSON to TQL
                                    parameters.put(ImplicitParameters.FILTER.getKey(),
                                            translator.toTQL(parameters.get(ImplicitParameters.FILTER.getKey())));
                                    filterColumns = tqlFilterService.getFilterColumnsMetadata(
                                            parameters.get(ImplicitParameters.FILTER.getKey()),
                                            stepRowMetadata.getRowMetadata());
                                }
                                // add metadata of the scope column if not already added(use when there is a column
                                // rename for example)
                                if (filterColumns
                                        .stream()
                                        .filter(column -> parameters
                                                .get(ImplicitParameters.COLUMN_ID.getKey())
                                                .equals(column.getId()))
                                        .findFirst()
                                        .orElse(null) == null) {
                                    filterColumns.addAll(stepRowMetadata
                                            .getRowMetadata()
                                            .getColumns()
                                            .stream()
                                            .filter(column -> parameters
                                                    .get(ImplicitParameters.COLUMN_ID.getKey())
                                                    .equals(column.getId()))
                                            .collect(Collectors.toList()));
                                }
                            }
                        } else {
                            filterColumns = target.getRowMetadata().getColumns();
                        }
                        action.setFilterColumns(filterColumns);
                    }
                    target.setActions(prepActions.getActions());

                    // Allow distributed run
                    boolean allowDistributedRun = true;
                    for (Action action : actions) {
                        final ActionDefinition actionDefinition = actionRegistry.get(action.getName());
                        if (actionDefinition
                                .getBehavior(action)
                                .contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED)) {
                            allowDistributedRun = false;
                            break;
                        }
                    }
                    target.setAllowDistributedRun(allowDistributedRun);

                    // no need to have lock information (and may also break StandAlonePreparationParser...)
                    target.setLock(null);

                    // Actions metadata
                    if (actionRegistry == null) {
                        LOGGER.debug(
                                "No action metadata available, unable to serialize action metadata for preparation {}.",
                                source.id());
                    } else {
                        List<ActionForm> actionDefinitions =
                                actions
                                        .stream() //
                                        .map(a -> actionRegistry
                                                .get(a.getName()) //
                                                .adapt(ScopeCategory.from(
                                                        a.getParameters().get(ImplicitParameters.SCOPE.getKey())))) //
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
}
