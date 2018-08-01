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

package org.talend.dataprep.preparation.service;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_DOES_NOT_EXIST;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_NAME_ALREADY_USED;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_NOT_EMPTY;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_ROOT_STEP_CANNOT_BE_DELETED;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_STEP_CANNOT_BE_REORDERED;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_STEP_DOES_NOT_EXIST;
import static org.talend.dataprep.folder.store.FoldersRepositoriesConstants.PATH_SEPARATOR;
import static org.talend.dataprep.i18n.DataprepBundle.message;
import static org.talend.dataprep.preparation.service.PreparationSearchCriterion.filterPreparation;
import static org.talend.dataprep.util.SortAndOrderHelper.getPreparationComparator;
import static org.talend.tql.api.TqlBuilder.and;
import static org.talend.tql.api.TqlBuilder.eq;
import static org.talend.tql.api.TqlBuilder.match;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.preparation.PreparationDetailsDTO;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.api.preparation.StepRowMetadata;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.conversions.inject.OwnerInjection;
import org.talend.dataprep.dataset.adapter.DatasetClient;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.PreparationErrorCodes;
import org.talend.dataprep.exception.json.JsonErrorCodeDescription;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.lock.store.LockedResourceRepository;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.actions.datablending.Lookup;
import org.talend.dataprep.transformation.api.action.validation.ActionMetadataValidation;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;
import org.talend.tql.api.TqlBuilder;
import org.talend.tql.model.Expression;

@Service
public class PreparationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationService.class);

    private static final String STEP_ID = "stepId";

    private final ActionFactory factory = new ActionFactory();

    /**
     * Where preparation are stored.
     */
    @Autowired
    protected PreparationRepository preparationRepository;

    /**
     * DataPrep abstraction to the underlying security (whether it's enabled or not).
     */
    @Autowired
    protected Security security;

    @Autowired
    private org.springframework.context.ApplicationContext springContext;

    /**
     * Where the folders are stored.
     */
    @Autowired
    private FolderRepository folderRepository;

    /**
     * Action validator.
     */
    @Autowired
    private ActionMetadataValidation validator;

    /**
     * Version service.
     */
    @Autowired
    private VersionService versionService;

    /**
     * Where all the actions are registered.
     */
    @Autowired
    private ActionRegistry actionRegistry;

    @Autowired
    private LockedResourceRepository lockedResourceRepository;

    @Autowired
    private MetadataChangesOnActionsGenerator stepDiffDelegate;

    @Autowired
    private ReorderStepsUtils reorderStepsUtils;

    @Autowired
    private BeanConversionService beanConversionService;

    @Autowired
    private PreparationUtils preparationUtils;

    @Autowired
    private DatasetClient datasetClient;

    /**
     * Create a preparation from the http request body.
     *
     * @param preparation the preparation to create.
     * @param folderId where to store the preparation.
     * @return the created preparation id.
     */
    public String create(final Preparation preparation, String folderId) {
        LOGGER.debug("Create new preparation for data set {} in {}", preparation.getDataSetId(), folderId);

        Preparation toCreate = new Preparation(UUID.randomUUID().toString(), versionService.version().getVersionId());
        toCreate.setHeadId(Step.ROOT_STEP.id());
        toCreate.setAuthor(security.getUserId());
        toCreate.setName(preparation.getName());
        toCreate.setDataSetId(preparation.getDataSetId());
        toCreate.setRowMetadata(preparation.getRowMetadata());
        try {
            toCreate.setDataSetName(datasetClient.getDataSetMetadata(preparation.getDataSetId()).getName());
        } catch (Exception e) {
            LOGGER.warn("Unable to find dataset name for preparation '{}'", preparation.getId());
        }

        preparationRepository.add(toCreate);

        final String id = toCreate.id();

        // create associated folderEntry
        FolderEntry folderEntry = new FolderEntry(PREPARATION, id);
        folderRepository.addFolderEntry(folderEntry, folderId);

        LOGGER.info("New preparation {} created and stored in {} ", preparation, folderId);
        return id;
    }

    /**
     * List all preparation details.
     *
     * @param name of the preparation.
     * @param folderPath filter on the preparation path.
     * @param path preparation full path in the form folder_path/preparation_name. Overrides folderPath and name if
     * present.
     * @param sort how to sort the preparations.
     * @param order how to order the sort.
     * @return the preparation details.
     */
    public Stream<PreparationDTO> listAll(String name, String folderPath, String path, Sort sort, Order order) {
        if (path != null) {
            // Transform path argument into folder path + preparation name
            if (path.contains(PATH_SEPARATOR.toString())) {
                // Special case the path should start with /
                if (!path.startsWith(PATH_SEPARATOR.toString())) {
                    path = PATH_SEPARATOR.toString().concat(path);
                }
                folderPath = StringUtils.substringBeforeLast(path, PATH_SEPARATOR.toString());
                // Special case if the preparation is in the root folder
                if (org.apache.commons.lang3.StringUtils.isEmpty(folderPath)) {
                    folderPath = PATH_SEPARATOR.toString();
                }
                name = StringUtils.substringAfterLast(path, PATH_SEPARATOR.toString());
            } else {
                // the preparation is in the root folder
                folderPath = PATH_SEPARATOR.toString();
                name = path;
                LOGGER.warn("Using path argument without '{}'. {} filter has been transformed into {}{}.",
                        PATH_SEPARATOR, path, PATH_SEPARATOR, name);
            }
        }

        return listAll(filterPreparation().byName(name).withNameExactMatch(true).byFolderPath(folderPath), sort, order);
    }

    public Stream<PreparationDTO> listAll(PreparationSearchCriterion searchCriterion, Sort sort, Order order) {
        LOGGER.debug("Get list of preparations (with details).");
        Stream<PersistentPreparation> preparationStream;

        if (searchCriterion.getName() == null && searchCriterion.getDataSetId() == null) {
            preparationStream = preparationRepository.list(PersistentPreparation.class);
        } else {
            Expression filter = null;
            if (searchCriterion.getName() != null) {
                filter = getNameFilter(searchCriterion.getName(), searchCriterion.isNameExactMatch());
            }
            if (searchCriterion.getDataSetId() != null) {
                Expression dataSetFilter = eq("dataSetId", searchCriterion.getDataSetId());
                filter = filter == null ? dataSetFilter : and(filter, dataSetFilter);
            }

            preparationStream = preparationRepository.list(PersistentPreparation.class, filter);
        }

        // filter on folder id
        if (searchCriterion.getFolderId() != null) {
            final Set<String> entries = folderRepository
                    .entries(searchCriterion.getFolderId(), PREPARATION) //
                    .map(FolderEntry::getContentId) //
                    .collect(Collectors.toSet());
            preparationStream = preparationStream.filter(p -> entries.contains(p.id())).peek(p -> p.setFolderId(searchCriterion.getFolderId()));
        }
        // filter on folder path
        if (searchCriterion.getFolderPath() != null) {
            final Optional<Folder> folder = folderRepository.getFolder(searchCriterion.getFolderPath());
            final Set<String> folderEntries = new HashSet<>();
            folder.ifPresent(f -> folderEntries.addAll(folderRepository
                    .entries(f.getId(), PREPARATION) //
                    .map(FolderEntry::getContentId) //
                    .collect(Collectors.toSet())));
            preparationStream = preparationStream.filter(p -> folderEntries.contains(p.id()));
        }

        final OwnerInjection ownerInjection = springContext.getBean(OwnerInjection.class);
        return preparationStream
                .map(p -> beanConversionService.convert(p, PreparationDTO.class, ownerInjection)) //
                .sorted(getPreparationComparator(sort, order));
    }

    /**
     * List all preparation summaries.
     *
     * @return the preparation summaries, sorted by descending last modification date.
     */
    public Stream<PreparationDTO> listSummary(String name, String folderPath, String path, Sort sort, Order order) {
        return listAll(name, folderPath, path, sort, order);
    }

    /**
     * <p>
     * Search preparation entry point.
     * </p>
     * <p>
     * <p>
     * So far at least one search criteria can be processed at a time among the following ones :
     * <ul>
     * <li>dataset id</li>
     * <li>preparation name & exact match</li>
     * <li>folderId path</li>
     * </ul>
     * </p>
     *
     * @param dataSetId to search all preparations based on this dataset id.
     * @param folderId to search all preparations located in this folderId.
     * @param name to search all preparations that match this name.
     * @param exactMatch if true, the name matching must be exact.
     * @param path
     * @param sort Sort key (by name, creation date or modification date).
     * @param order Order for sort key (desc or asc).
     */
    public Stream<PreparationDTO> searchPreparations(String dataSetId, String folderId, String name, boolean exactMatch,
            String path, Sort sort, Order order) {
        return listAll( //
                filterPreparation() //
                        .byDataSetId(dataSetId) //
                        .byFolderId(folderId) //
                        .byName(name) //
                        .withNameExactMatch(exactMatch) //
                        .byFolderPath(path), //
                sort, order);
    }

    /**
     * List all the preparations that matches the given name.
     *
     * @param name the wanted preparation name.
     * @param exactMatch true if the name must match exactly.
     * @return all the preparations that matches the given name.
     */
    private Expression getNameFilter(String name, boolean exactMatch) {
        LOGGER.debug("looking for preparations with the name '{}' exact match is {}.", name, exactMatch);
        final String regex;
        // Add case insensitive and escape special characters in name
        final String regexMainPart = "(?i)" + Pattern.quote(name);
        if (exactMatch) {
            regex = "^" + regexMainPart + "$";
        } else {
            regex = "^.*" + regexMainPart + ".*$";
        }
        return match("name", regex);
    }

    /**
     * Copy the given preparation to the given name / folder ans returns the new if in the response.
     *
     * @param name the name of the copied preparation, if empty, the name is "orginal-preparation-name Copy"
     * @param destination the folder path where to copy the preparation, if empty, the copy is in the same folder.
     * @return The new preparation id.
     */
    public String copy(String preparationId, String name, String destination) {

        LOGGER.debug("copy {} to folder {} with {} as new name");

        Preparation original = preparationRepository.get(preparationId, Preparation.class);

        // if no preparation, there's nothing to copy
        if (original == null) {
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, build().put("id", preparationId));
        }

        // use a default name if empty (original name + " Copy" )
        final String newName;
        if (StringUtils.isBlank(name)) {
            newName = message("preparation.copy.newname", original.getName());
        } else {
            newName = name;
        }
        checkIfPreparationNameIsAvailable(destination, newName);

        // copy the Preparation : constructor set HeadId and
        Preparation copy = new Preparation(original);
        copy.setId(UUID.randomUUID().toString());
        copy.setName(newName);
        final long now = System.currentTimeMillis();
        copy.setCreationDate(now);
        copy.setLastModificationDate(now);
        copy.setAuthor(security.getUserId());

        cloneStepsListBetweenPreparations(original, copy);

        // Save preparation to repository
        preparationRepository.add(copy);
        String newId = copy.getId();

        // add the preparation into the folder
        FolderEntry folderEntry = new FolderEntry(PREPARATION, newId);
        folderRepository.addFolderEntry(folderEntry, destination);

        LOGGER.debug("copy {} to folder {} with {} as new name", preparationId, destination, name);
        return newId;
    }

    /**
     * Duplicate the list of steps and set it to the new preparation
     *
     * @param originalPrep the original preparation.
     * @param targetPrep the created preparation.
     */
    private void cloneStepsListBetweenPreparations(Preparation originalPrep, Preparation targetPrep) {

        // copy the preparation's steps
        List<Step> copyListSteps = new ArrayList<>();
        // in order to save the previous step
        final Deque<Step> previousSteps = new ArrayDeque<>(1);
        previousSteps.push(Step.ROOT_STEP);

        copyListSteps.add(Step.ROOT_STEP);
        copyListSteps.addAll(originalPrep
                .getSteps()
                .stream() //
                .skip(1) // Skip root step
                .map(originalStep -> {
                    final StepDiff diff = new StepDiff();
                    diff.setCreatedColumns(Collections.emptyList());

                    final Step createdStep = new Step(previousSteps.pop().id(), originalStep.getContent(),
                            originalStep.getAppVersion(), diff);

                    previousSteps.push(createdStep);
                    return createdStep;
                }) //
                .collect(Collectors.toList()));
        targetPrep.setSteps(copyListSteps);
        targetPrep.setHeadId(previousSteps.pop().id());

    }

    /**
     * Check if the name is available in the given folderId.
     *
     * @param folderId where to look for the name.
     * @param name the wanted preparation name.
     * @throws TDPException Preparation name already used (409) if there's already a preparation with this name in the
     * folderId.
     */
    private void checkIfPreparationNameIsAvailable(String folderId, String name) {

        // make sure the preparation does not already exist in the target folderId
        try (final Stream<FolderEntry> entries = folderRepository.entries(folderId, PREPARATION)) {
            entries.forEach(folderEntry -> {
                Preparation preparation = preparationRepository.get(folderEntry.getContentId(), Preparation.class);
                if (preparation != null && StringUtils.equals(name, preparation.getName())) {
                    final ExceptionContext context = build() //
                            .put("id", folderEntry.getContentId()) //
                            .put("folderId", folderId) //
                            .put("name", name);
                    throw new TDPException(PREPARATION_NAME_ALREADY_USED, context);
                }
            });
        }
    }

    /**
     * Move a preparation to an other folder.
     *
     * @param folder The original folder of the preparation.
     * @param destination The new folder of the preparation.
     * @param newName The new preparation name.
     */
    public void move(String preparationId, String folder, String destination, String newName) {
        LOGGER.debug("moving {} from {} to {} with the new name '{}'", preparationId, folder, destination, newName);

        // get and lock the preparation to move
        final PersistentPreparation original = lockPreparation(preparationId);
        try {
            // set the target name
            final String targetName = StringUtils.isEmpty(newName) ? original.getName() : newName;

            // first check if the name is already used in the target folder
            checkIfPreparationNameIsAvailable(destination, targetName);

            // rename the dataset only if we received a new name
            if (!targetName.equals(original.getName())) {
                original.setName(newName);
                preparationRepository.add(original);
            }

            // move the preparation
            FolderEntry folderEntry = new FolderEntry(PREPARATION, preparationId);
            folderRepository.moveFolderEntry(folderEntry, folder, destination);

            LOGGER.info("preparation {} moved from {} to {} with the new name {}", preparationId, folder, destination,
                    targetName);
        } finally {
            unlockPreparation(preparationId);
        }
    }

    /**
     * Delete the preparation that match the given id.
     *
     * @param preparationId the preparation id to delete.
     */
    public void delete(String preparationId) {

        LOGGER.debug("Deletion of preparation #{} requested.", preparationId);

        final PersistentPreparation preparationToDelete = lockPreparation(preparationId);
        try {
            preparationRepository.remove(preparationToDelete);

            // delete the associated folder entries
            try (final Stream<FolderEntry> entries = folderRepository.findFolderEntries(preparationId, PREPARATION)) {
                entries.forEach(e -> folderRepository.removeFolderEntry(e.getFolderId(), preparationId, PREPARATION));
                LOGGER.info("Deletion of preparation #{} done.", preparationId);
            }
        } finally {
            // Just in case remove failed
            unlockPreparation(preparationId);
        }
    }

    /**
     * Update a preparation.
     *
     * @param preparationId the preparation id to update.
     * @param preparation the updated preparation.
     * @return the updated preparation id.
     */
    public String update(String preparationId, final PreparationDTO preparation) {
        lockPreparation(preparationId);

        try {
            final PersistentPreparation previousPreparation = preparationRepository.get(preparationId, PersistentPreparation.class);
            LOGGER.debug("Updating preparation with id {}: {}", preparation.getId(), previousPreparation);

            PersistentPreparation updated = previousPreparation.merge(preparation);

            if (!updated.id().equals(preparationId)) {
                preparationRepository.remove(previousPreparation);
            }
            updated.setAppVersion(versionService.version().getVersionId());
            updated.setLastModificationDate(System.currentTimeMillis());
            preparationRepository.add(updated);

            LOGGER.info("Preparation {} updated -> {}", preparationId, updated);

            return updated.id();
        } finally {
            unlockPreparation(preparationId);
        }
    }

    /**
     * Copy the steps from the another preparation to this one.
     * <p>
     * This is only allowed if this preparation has no steps.
     *
     * @param id the preparation id to update.
     * @param from the preparation id to copy the steps from.
     */
    public void copyStepsFrom(String id, String from) {

        LOGGER.debug("copy steps from {} to {}", from, id);

        final Preparation preparationToUpdate = preparationRepository.get(id, Preparation.class);
        if (preparationToUpdate == null) {
            LOGGER.error("cannot update {} steps --> preparation not found in repository", id);
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, build().put("id", id));
        }

        // if the preparation is not empty (head != root step) --> 409
        if (!StringUtils.equals(preparationToUpdate.getHeadId(), Step.ROOT_STEP.id())) {
            LOGGER.error("cannot update {} steps --> preparation has already steps.");
            throw new TDPException(PREPARATION_NOT_EMPTY, build().put("id", id));
        }

        final Preparation referencePreparation = preparationRepository.get(from, Preparation.class);
        if (referencePreparation == null) {
            LOGGER.warn("cannot copy steps from {} to {} because the original preparation is not found", from, id);
            return;
        }
        cloneStepsListBetweenPreparations(referencePreparation, preparationToUpdate);

        preparationToUpdate.setLastModificationDate(new Date().getTime());
        preparationRepository.add(preparationToUpdate);

        LOGGER.info("clone steps from {} to {} done --> {}", from, id, preparationToUpdate);
    }

    /**
     * Return a preparation details.
     *
     * @param id the wanted preparation id.
     * @param stepId the optional step id.
     * @return the preparation details.
     */
    public PreparationDTO getPreparationDetails(String id, String stepId) {
        LOGGER.debug("Get content of preparation details for #{}.", id);
        final PersistentPreparation preparation = preparationRepository.get(id, PersistentPreparation.class);

        if (preparation == null) {
            throw new TDPException(PreparationErrorCodes.PREPARATION_DOES_NOT_EXIST,
                    ExceptionContext.build().put("id", id));
        }

        ensurePreparationConsistency(preparation);

        // specify the step id if provided
        if (!StringUtils.equals("head", stepId)) {
            // just make sure the step does exist
            if (Step.ROOT_STEP.id().equals(stepId)) {
                preparation.setSteps(Collections.singletonList(Step.ROOT_STEP.id()));
                preparation.setHeadId(Step.ROOT_STEP.id());
            } else if (preparationRepository.exist(PersistentStep.class, TqlBuilder.eq("id", stepId))) {
                preparation.setSteps(preparationUtils.listStepsIds(stepId, preparationRepository));
                preparation.setHeadId(stepId);
            } else {
                throw new TDPException(PREPARATION_STEP_DOES_NOT_EXIST,
                        build().put("id", preparation).put(STEP_ID, stepId));
            }
        }

        final PreparationDTO details = beanConversionService.convert(preparation, PreparationDTO.class);
        LOGGER.debug("returning details for {} -> {}", id, details);
        return details;
    }

    /**
     * Return a preparation details.
     *
     * @param id the wanted preparation id.
     * @param stepId the optional step id.
     * @return the preparation details.
     */
    public PreparationDetailsDTO getPreparationDetailsFull(String id, String stepId) {

        final PreparationDTO prep = getPreparationDetails(id, stepId);

        final PreparationDetailsDTO details = beanConversionService.convert(prep, PreparationDetailsDTO.class);
        LOGGER.debug("returning details for {} -> {}", id, details);
        return details;
    }

    /**
     * This method ensures the consistency of a preparation .i.e. makes sure that a non-empty head step of a preparation
     * has its corresponding actions available. If it is not the case, we walk recursively on the steps from the current
     * head
     * to the root step until we reach a step having its actions accessible or we reach the root step.
     *
     * @param preparation the specified preparation
     */
    private void ensurePreparationConsistency(PersistentPreparation preparation) {
        final String headId = preparation.getHeadId();
        PersistentStep head = preparationRepository.get(headId, PersistentStep.class);
        if (head != null) {
            PreparationActions prepActions = preparationRepository.get(head.getContent(), PreparationActions.class);
            boolean inconsistentPreparation = false;
            while (prepActions == null && !head.getId().equals(Step.ROOT_STEP.id())) {
                LOGGER.info(
                        "Head step {} is inconsistent. Its corresponding action is unavailable. for the sake of safety new head is set to {}",
                        head.getId(), head.getParentId());

                inconsistentPreparation = true;
                deleteAction(preparation, head.getId());
                head = preparationRepository.get(head.getParentId(), PersistentStep.class);
                prepActions = preparationRepository.get(head.getContent(), PreparationActions.class);
            }

            if (inconsistentPreparation) {
                setPreparationHead(preparation, head);
            }
        }
    }

    /**
     * Return the folder that holds this preparation.
     *
     * @param id the wanted preparation id.
     * @return the folder that holds this preparation.
     */
    public Folder searchLocation(String id) {

        LOGGER.debug("looking the folder for {}", id);

        final Folder folder = folderRepository.locateEntry(id, PREPARATION);
        if (folder == null) {
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, build().put("id", id));
        }

        LOGGER.info("found where {} is stored : {}", id, folder);

        return folder;
    }

    public List<String> getSteps(String id) {
        LOGGER.debug("Get steps of preparation for #{}.", id);
        final PersistentStep step = getStep(id);
        return preparationUtils.listStepsIds(step.id(), preparationRepository);
    }

    public void addPreparationAction(final String preparationId, final AppendStep step) {
        LOGGER.debug("Adding action to preparation...");
        Preparation preparation = preparationRepository.get(preparationId, Preparation.class);
        List<Action> actions = getVersionedAction(preparationId, "head");
        StepDiff actionCreatedColumns = stepDiffDelegate.computeCreatedColumns(preparation.getRowMetadata(),
                buildActions(actions), buildActions(step.getActions()));
        step.setDiff(actionCreatedColumns);
        appendSteps(preparationId, Collections.singletonList(step));
        LOGGER.debug("Added action to preparation.");
    }

    /**
     * Given a list of actions recreate but with the Spring Context {@link ActionDefinition}. It is mandatory to use any
     * action parsed from JSON.
     */
    private List<RunnableAction> buildActions(List<Action> allActions) {
        final List<RunnableAction> builtActions = new ArrayList<>(allActions.size() + 1);
        for (Action parsedAction : allActions) {
            if (parsedAction != null && parsedAction.getName() != null) {
                String actionNameLowerCase = parsedAction.getName().toLowerCase();
                final ActionDefinition metadata = actionRegistry.get(actionNameLowerCase);
                builtActions.add(factory.create(metadata, parsedAction.getParameters()));
            }
        }
        return builtActions;
    }

    /**
     * Append step(s) in a preparation.
     */
    public void appendSteps(String preparationId, final List<AppendStep> stepsToAppend) {
        stepsToAppend.forEach(this::checkActionStepConsistency);

        LOGGER.debug("Adding actions to preparation #{}", preparationId);

        final PersistentPreparation preparation = lockPreparation(preparationId);
        try {
            LOGGER.debug("Current head for preparation #{}: {}", preparationId, preparation.getHeadId());

            // rebuild history from head
            replaceHistory(preparation, preparation.getHeadId(), stepsToAppend);
            LOGGER.debug("Added head to preparation #{}: head is now {}", preparationId, preparation.getHeadId());
        } finally {
            unlockPreparation(preparationId);
        }
    }

    /**
     * Update a step in a preparation <b>Strategy</b><br/>
     * The goal here is to rewrite the preparation history from 'the step to modify' (STM) to the head, with STM
     * containing the new action.<br/>
     * <ul>
     * <li>1. Extract the actions from STM (excluded) to the head</li>
     * <li>2. Insert the new actions before the other extracted actions. The actions list contains all the actions from
     * the <b>NEW</b> STM to the head</li>
     * <li>3. Set preparation head to STM's parent, so STM will be excluded</li>
     * <li>4. Append each action (one step is created by action) after the new preparation head</li>
     * </ul>
     */
    public void updateAction(final String preparationId, final String stepToModifyId, final AppendStep newStep) {
        checkActionStepConsistency(newStep);
        LOGGER.debug("Modifying actions in preparation #{}", preparationId);

        final PersistentPreparation preparation = lockPreparation(preparationId);
        try {
            LOGGER.debug("Current head for preparation #{}: {}", preparationId, preparation.getHeadId());

            // Get steps from "step to modify" to the head
            final List<String> steps = extractSteps(preparation, stepToModifyId); // throws an exception if stepId is
                                                                                  // not in
            // the preparation
            LOGGER.debug("Rewriting history for {} steps.", steps.size());

            // Extract created columns ids diff info
            final PersistentStep stm = getStep(stepToModifyId);
            final List<String> originalCreatedColumns = stm.getDiff().getCreatedColumns();
            final List<String> updatedCreatedColumns = newStep.getDiff().getCreatedColumns();
            final List<String> deletedColumns = originalCreatedColumns
                    .stream() // columns that the step was creating but
                    // not anymore
                    .filter(id -> !updatedCreatedColumns.contains(id))
                    .collect(toList());
            final int columnsDiffNumber = updatedCreatedColumns.size() - originalCreatedColumns.size();
            final int maxCreatedColumnIdBeforeUpdate = !originalCreatedColumns.isEmpty()
                    ? originalCreatedColumns.stream().mapToInt(Integer::parseInt).max().getAsInt()
                    : MAX_VALUE;

            // Build list of actions from modified one to the head
            final List<AppendStep> actionsSteps = getStepsWithShiftedColumnIds(steps, stepToModifyId, deletedColumns,
                    maxCreatedColumnIdBeforeUpdate, columnsDiffNumber);
            actionsSteps.add(0, newStep);

            // Rebuild history from modified step
            final PersistentStep stepToModify = getStep(stepToModifyId);
            replaceHistory(preparation, stepToModify.getParentId(), actionsSteps);
            LOGGER.debug("Modified head of preparation #{}: head is now {}", preparation.getHeadId());
        } finally {
            unlockPreparation(preparationId);
        }
    }

    /**
     * Delete a step in a preparation.<br/>
     * STD : Step To Delete <br/>
     * <br/>
     * <ul>
     * <li>1. Extract the actions from STD (excluded) to the head. The actions list contains all the actions from the
     * STD's child to the head.</li>
     * <li>2. Filter the actions that apply on a column created by the step to delete. Those steps will be removed
     * too.</li>
     * <li>2bis. Change the actions that apply on columns whose id is higher than the STD last created column id. The
     * created columns ids after
     * the STD are shifted.</li>
     * <li>3. Set preparation head to STD's parent, so STD will be excluded</li>
     * <li>4. Append each action after the new preparation head</li>
     * </ul>
     *
     * @param id the preparation id.
     * @param stepToDeleteId the step id to delete.
     */
    public void deleteAction(final String id, final String stepToDeleteId) {
        if (Step.ROOT_STEP.getId().equals(stepToDeleteId)) {
            throw new TDPException(PREPARATION_ROOT_STEP_CANNOT_BE_DELETED);
        }

        final PersistentPreparation preparation = lockPreparation(id);
        try {
            deleteAction(preparation, stepToDeleteId);
        } finally {
            unlockPreparation(id);
        }

    }

    public void setPreparationHead(final String preparationId, final String headId) {
        final PersistentStep head = getStep(headId);
        if (head == null) {
            throw new TDPException(PREPARATION_STEP_DOES_NOT_EXIST,
                    build().put("id", preparationId).put(STEP_ID, headId));
        }

        final PersistentPreparation preparation = lockPreparation(preparationId);
        try {
            setPreparationHead(preparation, head);
        } finally {
            unlockPreparation(preparationId);
        }
    }

    /**
     * Get all the actions of a preparation at given version.
     *
     * @param id the wanted preparation id.
     * @param version the wanted preparation version.
     * @return the list of actions.
     */
    public List<Action> getVersionedAction(final String id, final String version) {
        LOGGER.debug("Get list of actions of preparation #{} at version {}.", id, version);

        final PersistentPreparation preparation = preparationRepository.get(id, PersistentPreparation.class);
        if (preparation != null) {
            final String stepId = getStepId(version, preparation);
            final PersistentStep step = getStep(stepId);
            if (step == null) {
                LOGGER.warn("Step '{}' no longer exist for preparation #{} at version '{}'", stepId,
                        preparation.getId(), version);
            }
            return getActions(step);
        } else {
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, build().put("id", id));
        }
    }

    /**
     * List all preparation related error codes.
     */
    public Iterable<JsonErrorCodeDescription> listErrors() {
        // need to cast the typed dataset errors into mock ones to use json parsing
        List<JsonErrorCodeDescription> errors = new ArrayList<>(PreparationErrorCodes.values().length);
        for (PreparationErrorCodes code : PreparationErrorCodes.values()) {
            errors.add(new JsonErrorCodeDescription(code));
        }
        return errors;
    }

    public boolean isDatasetUsedInPreparation(final String datasetId) {
        final boolean preparationUseDataSet = isDatasetBaseOfPreparation(datasetId);
        final boolean dataSetUsedInLookup = isDatasetUsedToLookupInPreparationHead(datasetId);
        return preparationUseDataSet || dataSetUsedInLookup;
    }

    private boolean isDatasetBaseOfPreparation(String datasetId) {
        return preparationRepository.exist(Preparation.class, eq("dataSetId", datasetId));
    }

    /** Check if the preparation uses this dataset in its head version. */
    private boolean isDatasetUsedToLookupInPreparationHead(String datasetId) {
        final String datasetParamName = Lookup.Parameters.LOOKUP_DS_ID.getKey();
        return preparationRepository
                .list(Preparation.class)
                .flatMap(p -> getVersionedAction(p.getId(), "head").stream())
                .filter(Objects::nonNull)
                .filter(a -> Objects.equals(a.getName(), Lookup.LOOKUP_ACTION_NAME))
                .anyMatch(a -> Objects.equals(datasetId, a.getParameters().get(datasetParamName)));
    }

    /**
     * Moves the step with specified <i>stepId</i> just after the step with <i>parentStepId</i> as identifier within the
     * specified
     * preparation.
     *
     * @param preparationId the id of the preparation containing the step to move
     * @param stepId the id of the step to move
     * @param parentStepId the id of the step which wanted as the parent of the step to move
     */
    public void moveStep(final String preparationId, String stepId, String parentStepId) {
        LOGGER.debug("Moving step {} after step {}, within preparation {}", stepId, parentStepId, preparationId);
        final PersistentPreparation preparation = lockPreparation(preparationId);
        try {
            reorderSteps(preparation, stepId, parentStepId);
        } finally {
            unlockPreparation(preparationId);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------GETTERS/EXTRACTORS------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------

    /**
     * Get the actual step id by converting "head" and "origin" to the hash
     *
     * @param version The version to convert to step id
     * @param preparation The preparation
     * @return The converted step Id
     */
    protected String getStepId(final String version, final PersistentPreparation preparation) {
        if ("head".equalsIgnoreCase(version)) { //$NON-NLS-1$
            return preparation.getHeadId();
        } else if ("origin".equalsIgnoreCase(version)) { //$NON-NLS-1$
            return Step.ROOT_STEP.id();
        }
        return version;
    }

    /**
     * Get actions list from root to the provided step
     *
     * @param step The step
     * @return The list of actions
     */
    protected List<Action> getActions(final PersistentStep step) {
        if (step == null) {
            // TDP-3893: Make code more resilient to deleted steps
            return Collections.emptyList();
        }
        final PreparationActions preparationActions =
                preparationRepository.get(step.getContent(), PreparationActions.class);
        if (preparationActions != null) {
            return new ArrayList<>(preparationActions.getActions());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get the step from id
     *
     * @param stepId The step id
     * @return The step with the provided id, might return <code>null</code> is step does not exist.
     * @see PreparationRepository#get(String, Class)
     */
    public PersistentStep getStep(final String stepId) {
        return preparationRepository.get(stepId, PersistentStep.class);
    }

    /**
     * Get preparation from id with null result check.
     *
     * @param preparationId The preparation id.
     * @return The preparation with the provided id
     * @throws TDPException when no preparation has the provided id
     */
    public PreparationDTO getPreparation(final String preparationId) {
        final PersistentPreparation preparation = preparationRepository.get(preparationId, PersistentPreparation.class);
        if (preparation == null) {
            LOGGER.error("Preparation #{} does not exist", preparationId);
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, build().put("id", preparationId));
        }
        return beanConversionService.convert(preparation, PreparationDTO.class);
    }

    /**
     * Extract all actions after a provided step
     *
     * @param stepsIds The steps list
     * @param afterStep The (excluded) step id where to start the extraction
     * @return The actions after 'afterStep' to the end of the list
     */
    private List<AppendStep> extractActionsAfterStep(final List<String> stepsIds, final String afterStep) {
        final int stepIndex = stepsIds.indexOf(afterStep);
        if (stepIndex == -1) {
            return emptyList();
        }

        final List<PersistentStep> steps;
        try (IntStream range = IntStream.range(stepIndex, stepsIds.size())) {
            steps = range.mapToObj(index -> getStep(stepsIds.get(index))).collect(toList());
        }

        final List<List<Action>> stepActions = steps.stream().map(this::getActions).collect(toList());

        try (IntStream filteredActions = IntStream.range(1, steps.size())) {
            return filteredActions.mapToObj(index -> {
                final List<Action> previous = stepActions.get(index - 1);
                final List<Action> current = stepActions.get(index);
                final PersistentStep step = steps.get(index);

                final AppendStep appendStep = new AppendStep();
                appendStep.setDiff(step.getDiff());
                appendStep.setActions(current.subList(previous.size(), current.size()));
                return appendStep;
            }).collect(toList());
        }
    }

    /**
     * Get the steps ids from a specific step to the head. The specific step MUST be defined as an existing step of the
     * preparation
     *
     * @param preparation The preparation
     * @param fromStepId The starting step id
     * @return The steps ids from 'fromStepId' to the head
     * @throws TDPException If 'fromStepId' is not a step of the provided preparation
     */
    private List<String> extractSteps(final PersistentPreparation preparation, final String fromStepId) {
        final List<String> steps =
                preparationUtils.listStepsIds(preparation.getHeadId(), fromStepId, preparationRepository);
        if (!fromStepId.equals(steps.get(0))) {
            throw new TDPException(PREPARATION_STEP_DOES_NOT_EXIST,
                    build().put("id", preparation.getId()).put(STEP_ID, fromStepId));
        }
        return steps;
    }

    /**
     * Marks the specified preparation (identified by <i>preparationId</i>) as locked by the user identified by the
     * specified user (identified by <i>userId</i>).
     *
     * @param preparationId the specified preparation identifier
     * @throws TDPException if the lock is hold by another user
     */
    public PersistentPreparation lockPreparation(String preparationId) {
        return lockedResourceRepository.tryLock(preparationId, security.getUserId(), security.getUserDisplayName());
    }

    /**
     * Marks the specified preparation (identified by <i>preparationId</i>) as unlocked by the user identified by the
     * specified user (identified by <i>userId</i>).
     *
     * @param preparationId the specified preparation identifier
     * @throws TDPException if the lock is hold by another user
     */
    public void unlockPreparation(String preparationId) {
        lockedResourceRepository.unlock(preparationId, security.getUserId());
    }

    // ------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------CHECKERS-----------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------

    /**
     * Test if the stepId is the preparation head. Null, "head", "origin" and the actual step id are considered to be
     * the head
     *
     * @param preparation The preparation to test
     * @param stepId The step id to test
     * @return True if 'stepId' is considered as the preparation head
     */
    private boolean isPreparationHead(final PersistentPreparation preparation, final String stepId) {
        return stepId == null || "head".equals(stepId) || "origin".equals(stepId)
                || preparation.getHeadId().equals(stepId);
    }

    /**
     * Check the action parameters consistency
     *
     * @param step the step to check
     */
    private void checkActionStepConsistency(final AppendStep step) {
        for (final Action stepAction : step.getActions()) {
            validator.checkScopeConsistency(actionRegistry.get(stepAction.getName()), stepAction.getParameters());
        }
    }

    // ------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------HISTORY------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------

    /**
     * Currently, the columns ids are generated sequentially. There are 2 cases where those ids change in a step :
     * <ul>
     * <li>1. when a step that creates columns is deleted (ex1 : columns '0009' and '0010').</li>
     * <li>2. when a step that creates columns is updated : it can create more (add) or less (remove) columns. (ex2 :
     * add column '0009', '0010' + '0011' --> add 1 column)</li>
     * </ul>
     * In those cases, we have to
     * <ul>
     * <li>remove all steps that has action on a deleted column</li>
     * <li>shift all columns created after this step (ex1: columns > '0010', ex2: columns > '0011') by the number of
     * columns diff (ex1: remove 2 columns --> shift -2, ex2: add 1 column --> shift +1)</li>
     * <li>shift all actions that has one of the deleted columns as parameter (ex1: columns > '0010', ex2: columns >
     * '0011') by the number of columns diff (ex1: remove 2 columns --> shift -2, ex2: add 1 column --> shift +1)</li>
     * </ul>
     * <p>
     * 1. Get the steps with ids after 'afterStepId' 2. Rule 1 : Remove (filter) the steps which action is on one of the
     * 'deletedColumns' 3. Rule 2 : For all actions on columns ids > 'shiftColumnAfterId', we shift the column_id
     * parameter with a 'columnShiftNumber' value. (New_column_id = column_id + columnShiftNumber, only if column_id >
     * 'shiftColumnAfterId') 4. Rule 3 : The columns created AFTER 'shiftColumnAfterId' are shifted with the same rules
     * as rule 2. (New_created_column_id = created_column_id + columnShiftNumber, only if created_column_id >
     * 'shiftColumnAfterId')
     *
     * @param stepsIds The steps ids
     * @param afterStepId The (EXCLUDED) step where the extraction starts
     * @param deletedColumns The column ids that will be removed
     * @param shiftColumnAfterId The (EXCLUDED) column id where we start the shift
     * @param shiftNumber The shift number. new_column_id = old_columns_id + columnShiftNumber
     * @return The adapted steps
     */
    private List<AppendStep> getStepsWithShiftedColumnIds(final List<String> stepsIds, final String afterStepId,
            final List<String> deletedColumns, final int shiftColumnAfterId, final int shiftNumber) {
        Stream<AppendStep> stream = extractActionsAfterStep(stepsIds, afterStepId).stream();

        // rule 1 : remove all steps that modify one of the created columns
        if (!deletedColumns.isEmpty()) {
            stream = stream.filter(stepColumnIsNotIn(deletedColumns));
        }

        // when there is nothing to shift, we just return the filtered steps to avoid extra code
        if (shiftNumber == 0) {
            return stream.collect(toList());
        }

        // rule 2 : we have to shift all columns ids created after the step to delete/modify, in the column_id
        // parameters
        // For example, if the step to delete/modify creates columns 0010 and 0011, all steps that apply to column 0012
        // should now apply to 0012 - (2 created columns) = 0010
        stream = stream.map(shiftStepParameter(shiftColumnAfterId, shiftNumber));

        // rule 3 : we have to shift all columns ids created after the step to delete, in the steps diff
        stream = stream.map(shiftCreatedColumns(shiftColumnAfterId, shiftNumber));

        return stream.collect(toList());
    }

    /**
     * When the step diff created column ids > 'shiftColumnAfterId', we shift it by +columnShiftNumber (that wan be
     * negative)
     *
     * @param shiftColumnAfterId The shift is performed if created column id > shiftColumnAfterId
     * @param shiftNumber The number to shift (can be negative)
     * @return The same step but modified
     */
    private Function<AppendStep, AppendStep> shiftCreatedColumns(final int shiftColumnAfterId, final int shiftNumber) {

        final DecimalFormat format = new DecimalFormat("0000"); //$NON-NLS-1$
        return step -> {
            final List<String> stepCreatedCols = step.getDiff().getCreatedColumns();
            final List<String> shiftedStepCreatedCols = stepCreatedCols.stream().map(colIdStr -> {
                final int columnId = Integer.parseInt(colIdStr);
                if (columnId > shiftColumnAfterId) {
                    return format.format(columnId + (long) shiftNumber);
                }
                return colIdStr;
            }).collect(toList());
            step.getDiff().setCreatedColumns(shiftedStepCreatedCols);
            return step;
        };
    }

    /**
     * When the step column_id parameter > 'shiftColumnAfterId', we shift it by +columnShiftNumber (that wan be
     * negative)
     *
     * @param shiftColumnAfterId The shift is performed if column id > shiftColumnAfterId
     * @param shiftNumber The number to shift (can be negative)
     * @return The same step but modified
     */
    private Function<AppendStep, AppendStep> shiftStepParameter(final int shiftColumnAfterId, final int shiftNumber) {
        final DecimalFormat format = new DecimalFormat("0000"); //$NON-NLS-1$
        return step -> {
            final Action firstAction = step.getActions().get(0);
            final Map<String, String> parameters = firstAction.getParameters();
            if (parameters.get(ImplicitParameters.COLUMN_ID.getKey()) == null) {
                // this action is not applied on a column so no need to do a shift
                return step;
            }
            final int columnId = Integer.parseInt(parameters.get(ImplicitParameters.COLUMN_ID.getKey()));
            if (columnId > shiftColumnAfterId) {
                parameters.put(ImplicitParameters.COLUMN_ID.getKey(), format.format(columnId + (long) shiftNumber)); //$NON-NLS-1$
            }
            return step;
        };
    }

    /***
     * Predicate that returns if a step action is NOT on one of the columns list
     *
     * @param columns The columns ids list
     */
    private Predicate<AppendStep> stepColumnIsNotIn(final List<String> columns) {
        return step -> {
            final String columnId = step.getActions().get(0).getParameters().get("column_id"); //$NON-NLS-1$
            return columnId == null || !columns.contains(columnId);
        };
    }

    /**
     * Update the head step of a preparation
     *
     * @param preparation The preparation to update
     * @param head The head step
     */
    private void setPreparationHead(final PersistentPreparation preparation, final PersistentStep head) {
        preparation.setHeadId(head.id());
        preparation.setLastModificationDate(System.currentTimeMillis());
        preparation.setSteps(preparationUtils.listStepsIds(head.id(), preparationRepository));
        preparationRepository.add(preparation);
    }

    /**
     * Rewrite the preparation history from a specific step, with the provided actions
     *
     * @param preparation The preparation
     * @param startStepId The step id to start the (re)write. The following steps will be erased
     * @param actionsSteps The actions to perform
     */
    private void replaceHistory(final PersistentPreparation preparation, final String startStepId,
            final List<AppendStep> actionsSteps) {
        // move preparation head to the starting step
        if (!isPreparationHead(preparation, startStepId)) {
            final PersistentStep startingStep = getStep(startStepId);
            preparation
                    .setSteps(preparation.getSteps().subList(0, preparation.getSteps().indexOf(startingStep.id()) + 1));
            setPreparationHead(preparation, startingStep);
        }

        actionsSteps.forEach(step -> appendStepToHead(preparation, step));
    }

    /**
     * Append a single appendStep after the preparation head
     *
     * @param preparation The preparation.
     * @param appendStep The appendStep to apply.
     */
    private void appendStepToHead(final PersistentPreparation preparation, final AppendStep appendStep) {
        // Add new actions after head
        final String headId = preparation.getHeadId();
        final PersistentStep head = preparationRepository.get(headId, PersistentStep.class);
        final PreparationActions headActions = preparationRepository.get(head.getContent(), PreparationActions.class);
        final PreparationActions newContent = new PreparationActions();

        if (headActions == null) {
            LOGGER.info("Cannot retrieve the action corresponding to step {}. Therefore it will be skipped.", head);
            return;
        }
        final List<Action> newActions = new ArrayList<>(headActions.getActions());

        newActions.addAll(appendStep.getActions());
        newContent.setActions(newActions);

        // Create new step from new content
        final PersistentStep newHead = new PersistentStep();
        newHead.setParentId(headId);
        newHead.setId(UUID.randomUUID().toString());
        newHead.setContent(newContent.id());
        newHead.setDiff(appendStep.getDiff());
        preparation.getSteps().add(newHead.id());
        preparationRepository.add(newHead);
        preparationRepository.add(newContent);

        // Update preparation head step
        setPreparationHead(preparation, newHead);
    }

    /**
     * Deletes the step of specified id of the specified preparation
     *
     * @param preparation the specified preparation
     * @param stepToDeleteId the specified step id to delete
     */
    private void deleteAction(PersistentPreparation preparation, String stepToDeleteId) {
        final List<String> steps = extractSteps(preparation, stepToDeleteId); // throws an exception if stepId is not in

        // get created columns by step to delete
        final PersistentStep std = getStep(stepToDeleteId);
        final List<String> deletedColumns = std.getDiff().getCreatedColumns();
        final int columnsDiffNumber = -deletedColumns.size();
        final int maxCreatedColumnIdBeforeUpdate = deletedColumns.isEmpty() ? MAX_VALUE
                : deletedColumns.stream().mapToInt(Integer::parseInt).max().getAsInt();

        LOGGER.debug("Deleting actions in preparation #{} at step #{}", preparation.getId(), stepToDeleteId); //$NON-NLS-1$

        // get new actions to rewrite history from deleted step
        final List<AppendStep> actions = getStepsWithShiftedColumnIds(steps, stepToDeleteId, deletedColumns,
                maxCreatedColumnIdBeforeUpdate, columnsDiffNumber);

        // rewrite history
        final PersistentStep stepToDelete = getStep(stepToDeleteId);
        replaceHistory(preparation, stepToDelete.getParentId(), actions);
    }

    /**
     * Moves the step with specified <i>stepId</i> just after the step with <i>parentStepId</i> as identifier within the
     * specified preparation.
     *
     * @param preparation the preparation containing the step to move
     * @param stepId the id of the step to move
     * @param parentStepId the id of the step which wanted as the parent of the step to move
     */
    private void reorderSteps(final PersistentPreparation preparation, final String stepId, final String parentStepId) {
        final List<String> steps = extractSteps(preparation, Step.ROOT_STEP.getId());

        // extract all appendStep
        final List<AppendStep> allAppendSteps = extractActionsAfterStep(steps, steps.get(0));

        final int stepIndex = steps.indexOf(stepId);
        final int parentIndex = steps.indexOf(parentStepId);

        if (stepIndex < 0) {
            throw new TDPException(PREPARATION_STEP_DOES_NOT_EXIST,
                    build().put("id", preparation.getId()).put(STEP_ID, stepId));
        }
        if (parentIndex < 0) {
            throw new TDPException(PREPARATION_STEP_DOES_NOT_EXIST,
                    build().put("id", preparation.getId()).put(STEP_ID, parentStepId));
        }

        if (stepIndex - 1 == parentIndex) {
            LOGGER.debug(
                    "No need to Move step {} after step {}, within preparation {}: already at the wanted position.",
                    stepId, parentStepId, preparation.getId());
        } else {
            final int lastUnchangedIndex;

            if (parentIndex < stepIndex) {
                lastUnchangedIndex = parentIndex;
            } else {
                lastUnchangedIndex = stepIndex - 1;
            }

            final AppendStep removedStep = allAppendSteps.remove(stepIndex - 1);
            allAppendSteps.add(lastUnchangedIndex == stepIndex - 1 ? parentIndex - 1 : parentIndex, removedStep);

            // check that the wanted reordering is legal
            if (!reorderStepsUtils.isStepOrderValid(allAppendSteps)) {
                throw new TDPException(PREPARATION_STEP_CANNOT_BE_REORDERED, build());
            }

            // rename created columns to conform to the way the transformation are performed
            reorderStepsUtils.renameCreatedColumns(allAppendSteps);

            // apply the reordering since it seems to be legal
            final List<AppendStep> result = allAppendSteps.subList(lastUnchangedIndex, allAppendSteps.size());
            replaceHistory(preparation, steps.get(lastUnchangedIndex), result);
        }
    }

    public void updatePreparationStep(String stepId, RowMetadata rowMetadata) {
        final PersistentStep step = preparationRepository.get(stepId, PersistentStep.class);

        invalidatePreparationStep(stepId);

        // ...and create new one for step
        final StepRowMetadata stepRowMetadata = new StepRowMetadata(rowMetadata);
        preparationRepository.add(stepRowMetadata);
        step.setRowMetadata(stepRowMetadata.id());
        preparationRepository.add(step);
    }

    public void invalidatePreparationStep(String stepId) {
        final PersistentStep step = preparationRepository.get(stepId, PersistentStep.class);

        if (step.getRowMetadata() != null) {
            // Delete previous one...
            final StepRowMetadata previousStepRowMetadata = new StepRowMetadata();
            previousStepRowMetadata.setId(step.getRowMetadata());
            preparationRepository.remove(previousStepRowMetadata);
        }
    }

    public RowMetadata getPreparationStep(String stepId) {
        final PersistentStep step = preparationRepository.get(stepId, PersistentStep.class);
        if (step != null) {
            final StepRowMetadata stepRowMetadata =
                    preparationRepository.get(step.getRowMetadata(), StepRowMetadata.class);
            if (stepRowMetadata != null) {
                return stepRowMetadata.getRowMetadata();
            }
        }
        return null;
    }

}
