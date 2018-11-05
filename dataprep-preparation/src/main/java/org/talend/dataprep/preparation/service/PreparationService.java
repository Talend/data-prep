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
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
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
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.CREATE_NEW_COLUMN;
import static org.talend.dataprep.util.SortAndOrderHelper.getPreparationComparator;
import static org.talend.tql.api.TqlBuilder.and;
import static org.talend.tql.api.TqlBuilder.eq;
import static org.talend.tql.api.TqlBuilder.isEmpty;
import static org.talend.tql.api.TqlBuilder.match;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.action.ActionForm;
import org.talend.dataprep.api.dataset.ColumnMetadata;
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
import org.talend.dataprep.audit.BaseDataprepAuditService;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.conversions.inject.OwnerInjection;
import org.talend.dataprep.preparation.configuration.SharedInjection;
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
import org.talend.tql.model.Expression;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class PreparationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationService.class);

    private static final String STEP_ID = "stepId";

    private static final String DATASET_ID = "dataSetId";

    private static final String ID = "id";

    private static final String FOLDER_ID = "folderId";

    private static final String ORIGIN = "origin";

    private static final String HEAD = "head";

    private static final String NAME = "name";

    private static final String FIRST_COLUMN_INDEX = "0000";

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

    private final ActionFactory factory = new ActionFactory();

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

    @Autowired
    private OwnerInjection ownerInjection;

    @Autowired
    private SharedInjection sharedInjection;

    @Autowired
    private BaseDataprepAuditService auditService;

    @Autowired
    private DataSetNameInjection dataSetNameInjection;

    /**
     * Create a preparation from the http request body.
     *
     * @param preparation the preparation to create.
     * @param folderId    where to store the preparation.
     * @return the created preparation id.
     */
    public String create(final Preparation preparation, String folderId) {
        LOGGER.debug("Create new preparation for data set {} in {}", preparation.getDataSetId(), folderId);

        PersistentPreparation toCreate = new PersistentPreparation();
        toCreate.setId(UUID.randomUUID().toString());
        toCreate.setAppVersion(versionService.version().getVersionId());
        toCreate.setHeadId(Step.ROOT_STEP.id());
        toCreate.setAuthor(security.getUserId());
        toCreate.setName(preparation.getName());
        toCreate.setDataSetId(preparation.getDataSetId());
        toCreate.setFolderId(folderId);
        toCreate.setRowMetadata(preparation.getRowMetadata());
        try {
            toCreate.setDataSetName(datasetClient.getDataSetMetadata(preparation.getDataSetId()).getName());
        } catch (Exception e) {
            LOGGER.warn("Unable to find dataset name for preparation '{}'", preparation.getId(), e);
        }

        preparationRepository.add(toCreate);

        final String id = toCreate.id();

        // create associated folderEntry
        FolderEntry folderEntry = new FolderEntry(PREPARATION, id);
        folderRepository.addFolderEntry(folderEntry, folderId);

        auditService.auditPreparationCreation(toCreate.getName(), id, toCreate.getDataSetName(),
                toCreate.getDataSetId(), folderId);
        LOGGER.info("New preparation {} created and stored in {} ", preparation, folderId);
        return id;
    }

    /**
     * List all preparation details.
     *
     * @param name       of the preparation.
     * @param folderPath filter on the preparation path.
     * @param path       preparation full path in the form folder_path/preparation_name. Overrides folderPath and name if
     *                   present.
     * @param sort       how to sort the preparations.
     * @param order      how to order the sort.
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

        Expression filter = null;
        Predicate<PersistentPreparation> deprecatedFolderIdFilter = null;
        if (searchCriterion.getName() != null) {
            filter = getNameFilter(searchCriterion.getName(), searchCriterion.isNameExactMatch());
        }
        if (searchCriterion.getDataSetId() != null) {
            Expression dataSetFilter = eq(DATASET_ID, searchCriterion.getDataSetId());
            filter = filter == null ? dataSetFilter : and(filter, dataSetFilter);
        }
        if (searchCriterion.getFolderId() != null) {
            if (preparationRepository.exist(PersistentPreparation.class, isEmpty(FOLDER_ID))) {
                // filter on folder id (DEPRECATED VERSION - only applies if migration isn't completed yet)
                try (Stream<FolderEntry> folders =
                        folderRepository.entries(searchCriterion.getFolderId(), PREPARATION)) {
                    final Set<String> entries = folders
                            .map(FolderEntry::getContentId) //
                            .collect(toSet());
                    deprecatedFolderIdFilter = p -> entries.contains(p.id());
                }
            } else {
                // Once all preparations all have the folder id,
                Expression folderIdFilter = eq(FOLDER_ID, searchCriterion.getFolderId());
                filter = filter == null ? folderIdFilter : and(filter, folderIdFilter);
            }
        }

        // Handles filtering (prefer database filters)
        if (filter != null) {
            preparationStream = preparationRepository.list(PersistentPreparation.class, filter);
        } else {
            preparationStream = preparationRepository.list(PersistentPreparation.class);
        }
        // migration for preparation after the change from dataset ID to dataset name
        // see TDP-6195 and TDP-5696
        preparationStream = preparationStream.map(dataSetNameInjection::injectDatasetNameBasedOnId);

        if (deprecatedFolderIdFilter != null) {
            // filter on folder id (DEPRECATED VERSION - only applies if migration isn't completed yet)
            preparationStream = preparationStream //
                    .filter(deprecatedFolderIdFilter) //
                    .peek(p -> p.setFolderId(searchCriterion.getFolderId()));
        }

        // filter on folder path
        if (searchCriterion.getFolderPath() != null) {
            final Optional<Folder> folder = folderRepository.getFolder(searchCriterion.getFolderPath());
            final Set<String> folderEntries = new HashSet<>();
            folder.ifPresent(f -> {
                try (Stream<String> preparationIds = folderRepository //
                        .entries(f.getId(), PREPARATION) //
                        .map(FolderEntry::getContentId)) {
                    folderEntries.addAll(preparationIds //
                            .collect(toSet()));
                }
            });
            preparationStream = preparationStream.filter(p -> folderEntries.contains(p.id()));
        }

        return preparationStream
                .map(p -> beanConversionService.convert(p, PreparationDTO.class, ownerInjection.injectIntoPreparation(),
                        sharedInjection)) //
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
     * @param dataSetId  to search all preparations based on this dataset id.
     * @param folderId   to search all preparations located in this folderId.
     * @param name       to search all preparations that match this name.
     * @param exactMatch if true, the name matching must be exact.
     * @param path
     * @param sort       Sort key (by name, creation date or modification date).
     * @param order      Order for sort key (desc or asc).
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
     * @param name       the wanted preparation name.
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
        return match(NAME, regex);
    }

    /**
     * Copy the given preparation to the given name / folder ans returns the new if in the response.
     *
     * @param name        the name of the copied preparation, if empty, the name is "orginal-preparation-name Copy"
     * @param destination the folder path where to copy the preparation, if empty, the copy is in the same folder.
     * @return The new preparation id.
     */
    public String copy(String preparationId, String name, String destination) {

        LOGGER.debug("Copy {} preparation to folder '{}' with '{}' as new name requested.", preparationId, destination,
                name);

        Preparation original = preparationRepository.get(preparationId, Preparation.class);

        // if no preparation, there's nothing to copy
        if (original == null) {
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, build().put(ID, preparationId));
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
        copy.setDataSetName(original.getDataSetName());
        copy.setFolderId(destination);
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

        LOGGER.debug("Copy {} preparation to folder '{}' with '{}' as new copied name.", preparationId, destination,
                name);
        auditService.auditPreparationCopy(preparationId, destination, name, newId);
        return newId;
    }

    /**
     * Duplicate the list of steps and set it to the new preparation
     *
     * @param originalPrep the original preparation.
     * @param targetPrep   the created preparation.
     */
    private void cloneStepsListBetweenPreparations(Preparation originalPrep, Preparation targetPrep) {

        // copy the preparation's steps
        List<Step> copyListSteps = new ArrayList<>();
        // in order to save the previous step
        final Deque<Step> previousSteps = new ArrayDeque<>(1);
        previousSteps.push(Step.ROOT_STEP);

        copyListSteps.add(Step.ROOT_STEP);
        copyListSteps.addAll(originalPrep //
                .getSteps() //
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
                .collect(toList()));
        targetPrep.setSteps(copyListSteps);
        targetPrep.setHeadId(previousSteps.pop().id());

    }

    /**
     * Check if the name is available in the given folderId.
     *
     * @param folderId where to look for the name.
     * @param name     the wanted preparation name.
     * @throws TDPException Preparation name already used (409) if there's already a preparation with this name in the
     *                      folderId.
     */
    private void checkIfPreparationNameIsAvailable(String folderId, String name) {

        // make sure the preparation does not already exist in the target folderId
        try (final Stream<FolderEntry> entries = folderRepository.entries(folderId, PREPARATION)) {
            entries.forEach(folderEntry -> {
                Preparation preparation = preparationRepository.get(folderEntry.getContentId(), Preparation.class);
                if (preparation != null && StringUtils.equals(name, preparation.getName())) {
                    final ExceptionContext context = build() //
                            .put(ID, folderEntry.getContentId()) //
                            .put(FOLDER_ID, folderId) //
                            .put(NAME, name);
                    throw new TDPException(PREPARATION_NAME_ALREADY_USED, context);
                }
            });
        }
    }

    /**
     * Move a preparation to an other folder.
     *
     * @param folder      The original folder of the preparation.
     * @param destination The new folder of the preparation.
     * @param newName     The new preparation name.
     */
    public void move(String preparationId, String folder, String destination, String newName) {
        LOGGER.debug("Moving {} preparation from '{}' folder to '{}' folder with the new name '{}' requested",
                preparationId, folder, destination, newName);

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
            }
            original.setFolderId(destination);
            preparationRepository.add(original);

            // move the preparation
            FolderEntry folderEntry = new FolderEntry(PREPARATION, preparationId);
            folderRepository.moveFolderEntry(folderEntry, folder, destination);

            LOGGER.info("Preparation {} moved from '{}' folder to '{}' folder with the new name '{}'", preparationId,
                    folder, destination, targetName);
            auditService.auditPreparationMove(preparationId, folder, destination, targetName);
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
                auditService.auditPreparationDeletion(preparationId);
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
     * @param preparation   the updated preparation.
     * @return the updated preparation id.
     */
    public String update(String preparationId, final PreparationDTO preparation) {
        lockPreparation(preparationId);

        try {
            final PersistentPreparation previousPreparation =
                    preparationRepository.get(preparationId, PersistentPreparation.class);
            LOGGER.debug("Updating preparation with id {}: {}", preparation.getId(), previousPreparation);

            PersistentPreparation updated = previousPreparation.merge(preparation);

            if (!updated.id().equals(preparationId)) {
                preparationRepository.remove(previousPreparation);
            }
            updated.setAppVersion(versionService.version().getVersionId());
            updated.setLastModificationDate(System.currentTimeMillis());
            preparationRepository.add(updated);

            LOGGER.info("Preparation {} updated -> {}", preparationId, updated);
            auditService.auditPreparationRename(preparationId, updated.getName());

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
     * @param id   the preparation id to update.
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
            LOGGER.error("cannot update {} steps --> preparation has already steps.", id);
            throw new TDPException(PREPARATION_NOT_EMPTY, build().put(ID, id));
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
        auditService.auditPreparationCopySteps(from, referencePreparation.getName(), id, preparationToUpdate.getName());
    }

    /**
     * Return a preparation details.
     *
     * @param id     the wanted preparation id.
     * @param stepId the optional step id.
     * @return the preparation details.
     */
    public PreparationDTO getPreparationDetails(String id, String stepId) {
        LOGGER.debug("Get content of preparation details for #{}.", id);
        final PersistentPreparation preparation = preparationRepository.get(id, PersistentPreparation.class);

        if (preparation == null) {
            throw new TDPException(PreparationErrorCodes.PREPARATION_DOES_NOT_EXIST, build().put(ID, id));
        }

        ensurePreparationConsistency(preparation);

        // specify the step id if provided
        if (!StringUtils.equals(HEAD, stepId)) {
            // just make sure the step does exist
            if (Step.ROOT_STEP.id().equals(stepId)) {
                preparation.setSteps(Collections.singletonList(Step.ROOT_STEP.id()));
                preparation.setHeadId(Step.ROOT_STEP.id());
            } else if (preparationRepository.exist(PersistentStep.class, eq(ID, stepId))) {
                preparation.setSteps(preparationUtils.listStepsIds(stepId, preparationRepository));
                preparation.setHeadId(stepId);
            } else {
                throw new TDPException(PREPARATION_STEP_DOES_NOT_EXIST,
                        build().put(ID, preparation).put(STEP_ID, stepId));
            }
        }

        final PreparationDTO details = beanConversionService.convert(preparation, PreparationDTO.class);
        LOGGER.debug("returning details for {} -> {}", id, details);
        return details;
    }

    /**
     * Return a preparation details.
     *
     * @param id     the wanted preparation id.
     * @param stepId the optional step id.
     * @return the preparation details.
     */
    public PreparationDetailsDTO getPreparationDetailsFull(String id, String stepId) {
        final PreparationDTO prep = getPreparationDetails(id, stepId);
        final PreparationDetailsDTO details =
                injectActionsForms(beanConversionService.convert(prep, PreparationDetailsDTO.class));
        LOGGER.debug("returning details for {} -> {}", id, details);
        return details;
    }

    private PreparationDetailsDTO injectActionsForms(PreparationDetailsDTO details) {
        // Append actions and action forms
        Iterator<String> stepsIterator = details.getSteps().iterator();
        final AtomicBoolean allowDistributedRun = new AtomicBoolean();
        final List<ActionForm> metadata = details
                .getActions()
                .stream()
                .map(action -> {
                    String stepBeforeAction = stepsIterator.next();
                    return adaptActionDefinition(details, action, stepBeforeAction);
                })
                .peek(a -> {
                    if (allowDistributedRun.get()) {
                        allowDistributedRun.set(a.getBehavior().contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED));
                    }
                }) //
                .map(a -> a.getActionForm(LocaleContextHolder.getLocale(), Locale.US)) //
                .map(PreparationService::disallowColumnCreationChange) //
                .collect(toList());

        details.setMetadata(metadata);

        // Flag for allow distributed run (based on metadata).
        details.setAllowDistributedRun(allowDistributedRun.get());

        return details;
    }

    /**
     * Adapt the ActionDefinition to column it was applied to. Important to have the dependent parameters in the step
     * list in playground.
     */
    // Adapt to column as some actions won't have parameters if not adapted first (sigh*)
    private ActionDefinition adaptActionDefinition(PreparationDetailsDTO details, Action action,
            String stepBeforeAction) {
        actionRegistry.get(action.getName());
        Step step = preparationRepository.get(stepBeforeAction, Step.class);
        ActionDefinition actionDefinition = actionRegistry.get(action.getName());

        // first: fetches the column id parameter int the applied action
        String columnIdAsString = action.getParameters().get(ImplicitParameters.COLUMN_ID.getKey());
        if (columnIdAsString != null) {
            // Then fetches the column metadata for the id in parameter
            RowMetadata dataSetRowMetadata;
            if (Step.ROOT_STEP.equals(step)) {
                // If the parent step is root step we need to fetch row metadata in dataset
                dataSetRowMetadata = datasetClient.getDataSetRowMetadata(details.getDataSetId());
            } else {
                // if not, the step metadata should be cached in the repository
                String rowMetadataId = step.getRowMetadata();
                StepRowMetadata stepRowMetadata = preparationRepository.get(rowMetadataId, StepRowMetadata.class);
                if (stepRowMetadata == null) {
                    dataSetRowMetadata = null;
                } else {
                    dataSetRowMetadata = stepRowMetadata.getRowMetadata();
                }
            }

            if (dataSetRowMetadata != null) {
                ColumnMetadata column = dataSetRowMetadata.getById(columnIdAsString);
                if (column != null) {
                    return actionDefinition.adapt(column);
                }
            }
        }
        return actionDefinition;
    }

    /**
     * For a given action form, it will disallow edition on all column creation check. It is a safety specified in
     * TDP-4531 to
     * avoid removing columns used by other actions.
     * <p>
     * Such method is not ideal as the system should be able to handle such removal in a much more generic way.
     * </p>
     */
    private static ActionForm disallowColumnCreationChange(ActionForm form) {
        form
                .getParameters() //
                .stream() //
                .filter(p -> CREATE_NEW_COLUMN.equals(p.getName())) //
                .forEach(p -> p.setReadonly(true));
        return form;
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
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, build().put(ID, id));
        }

        LOGGER.info("found where {} is stored : {}", id, folder);

        return folder;
    }

    public List<String> getSteps(String id) {
        LOGGER.debug("Get steps of preparation for #{}.", id);
        final PersistentStep step = getStep(id);
        return preparationUtils.listStepsIds(step.id(), preparationRepository);
    }

    public void addPreparationAction(final String preparationId, final AppendStep appendStep) {
        PersistentPreparation preparation = preparationRepository.get(preparationId, PersistentPreparation.class);
        List<Action> actions = getVersionedAction(preparation, HEAD);
        StepDiff actionCreatedColumns = stepDiffDelegate.computeCreatedColumns(preparation.getRowMetadata(),
                buildActions(actions), buildActions(appendStep.getActions()));
        appendStep.setDiff(actionCreatedColumns);

        checkActionStepConsistency(appendStep);
        appendStepToHead(preparation, appendStep);

        LOGGER.debug("Added action to preparation.");
        if (auditService.isActive()) {
            auditService.auditPreparationAddStep(preparationId,
                    appendStep.getActions().stream().collect(toMap(Action::getName, Action::getParameters)));
        }
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
            final List<String> deletedColumns = originalCreatedColumns //
                    .stream() // columns that the step was creating but not anymore
                    .filter(id -> !updatedCreatedColumns.contains(id)) //
                    .collect(toList());
            final int columnsDiffNumber = updatedCreatedColumns.size() - originalCreatedColumns.size();
            final int maxCreatedColumnIdBeforeUpdate = !originalCreatedColumns.isEmpty() ? //
                    originalCreatedColumns.stream().mapToInt(Integer::parseInt).max().getAsInt() : MAX_VALUE;

            // Build list of actions from modified one to the head
            final List<AppendStep> actionsSteps = getStepsWithShiftedColumnIds(steps, stepToModifyId, deletedColumns,
                    maxCreatedColumnIdBeforeUpdate, columnsDiffNumber);
            actionsSteps.add(0, newStep);

            // Rebuild history from modified step
            final PersistentStep stepToModify = getStep(stepToModifyId);
            replaceHistory(preparation, stepToModify.getParentId(), actionsSteps);
            LOGGER.debug("Modified head of preparation #{}: head is now {}", preparationId, preparation.getHeadId());
            if (auditService.isActive()) {
                auditService //
                        .auditPreparationUpdateStep(preparationId, stepToModifyId,
                                newStep //
                                        .getActions() //
                                        .stream() //
                                        .collect(toMap(Action::getName, Action::getParameters)));
            }
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
     * @param id             the preparation id.
     * @param stepToDeleteId the step id to delete.
     */
    public void deleteAction(final String id, final String stepToDeleteId) {
        if (Step.ROOT_STEP.getId().equals(stepToDeleteId)) {
            throw new TDPException(PREPARATION_ROOT_STEP_CANNOT_BE_DELETED);
        }

        final PersistentPreparation preparation = lockPreparation(id);
        try {
            deleteAction(preparation, stepToDeleteId);
            auditService.auditPreparationDeleteStep(preparation.getId(), preparation.getName(), stepToDeleteId);
        } finally {
            unlockPreparation(id);
        }

    }

    public void setPreparationHead(final String preparationId, final String headId) {
        final PersistentStep head = getStep(headId);
        if (head == null) {
            throw new TDPException(PREPARATION_STEP_DOES_NOT_EXIST,
                    build().put(ID, preparationId).put(STEP_ID, headId));
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
     * @param id      the wanted preparation id.
     * @param version the wanted preparation version.
     * @return the list of actions.
     */
    public List<Action> getVersionedAction(final String id, final String version) {
        LOGGER.debug("Get list of actions of preparation #{} at version {}.", id, version);

        final PersistentPreparation preparation = preparationRepository.get(id, PersistentPreparation.class);
        if (preparation != null) {
            return getVersionedAction(preparation, version);
        } else {
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, build().put(ID, id));
        }
    }

    private List<Action> getVersionedAction(final PersistentPreparation preparation, final String version) {
        LOGGER.debug("Get list of actions of preparation #{} at version {}.", preparation.getId(), version);
        final String stepId = getStepId(version, preparation);
        final PersistentStep step = getStep(stepId);
        if (step == null) {
            LOGGER.warn("Step '{}' no longer exist for preparation #{} at version '{}'", stepId, preparation.getId(),
                    version);
        }
        return getActions(step);
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
        return preparationRepository.exist(Preparation.class, eq(DATASET_ID, datasetId));
    }

    /**
     * Check if the preparation uses this dataset in its head version.
     */
    private boolean isDatasetUsedToLookupInPreparationHead(String datasetId) {
        final String datasetParamName = Lookup.Parameters.LOOKUP_DS_ID.getKey();
        return preparationRepository //
                .list(Preparation.class) //
                .flatMap(p -> getVersionedAction(p.getId(), HEAD).stream()) //
                .filter(Objects::nonNull) //
                .filter(a -> Objects.equals(a.getName(), Lookup.LOOKUP_ACTION_NAME)) //
                .anyMatch(a -> Objects.equals(datasetId, a.getParameters().get(datasetParamName)));
    }

    /**
     * Moves the step with specified <i>stepId</i> just after the step with <i>parentStepId</i> as identifier within the
     * specified
     * preparation.
     *
     * @param preparationId the id of the preparation containing the step to move
     * @param stepId        the id of the step to move
     * @param parentStepId  the id of the step which wanted as the parent of the step to move
     */
    public void moveStep(final String preparationId, String stepId, String parentStepId) {
        LOGGER.debug("Moving step {} after step {}, within preparation {}", stepId, parentStepId, preparationId);
        final PersistentPreparation preparation = lockPreparation(preparationId);
        try {
            reorderSteps(preparation, stepId, parentStepId);
            auditService.auditPreparationMoveStep(preparationId, preparation.getName(), stepId, parentStepId);
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
     * @param version     The version to convert to step id
     * @param preparation The preparation
     * @return The converted step Id
     */
    protected String getStepId(final String version, final PersistentPreparation preparation) {
        if (HEAD.equalsIgnoreCase(version)) { // $NON-NLS-1$
            return preparation.getHeadId();
        } else if (ORIGIN.equalsIgnoreCase(version)) { // $NON-NLS-1$
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
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, build().put(ID, preparationId));
        }
        return beanConversionService.convert(preparation, PreparationDTO.class);
    }

    /**
     * Extract all actions after a provided step
     *
     * @param stepsIds  The steps list
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
     * @param fromStepId  The starting step id
     * @return The steps ids from 'fromStepId' to the head
     * @throws TDPException If 'fromStepId' is not a step of the provided preparation
     */
    private List<String> extractSteps(final PersistentPreparation preparation, final String fromStepId) {
        final List<String> steps =
                preparationUtils.listStepsIds(preparation.getHeadId(), fromStepId, preparationRepository);
        if (!fromStepId.equals(steps.get(0))) {
            throw new TDPException(PREPARATION_STEP_DOES_NOT_EXIST,
                    build().put(ID, preparation.getId()).put(STEP_ID, fromStepId));
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
     * @param stepId      The step id to test
     * @return True if 'stepId' is considered as the preparation head
     */
    private boolean isPreparationHead(final PersistentPreparation preparation, final String stepId) {
        return stepId == null || HEAD.equals(stepId) || ORIGIN.equals(stepId) || preparation.getHeadId().equals(stepId);
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
     * @param stepsIds           The steps ids
     * @param afterStepId        The (EXCLUDED) step where the extraction starts
     * @param deletedColumns     The column ids that will be removed
     * @param shiftColumnAfterId The (EXCLUDED) column id where we start the shift
     * @param shiftNumber        The shift number. new_column_id = old_columns_id + columnShiftNumber
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
     * @param shiftNumber        The number to shift (can be negative)
     * @return The same step but modified
     */
    private Function<AppendStep, AppendStep> shiftCreatedColumns(final int shiftColumnAfterId, final int shiftNumber) {

        final DecimalFormat format = new DecimalFormat(FIRST_COLUMN_INDEX); //$NON-NLS-1$
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
     * @param shiftNumber        The number to shift (can be negative)
     * @return The same step but modified
     */
    private Function<AppendStep, AppendStep> shiftStepParameter(final int shiftColumnAfterId, final int shiftNumber) {
        final DecimalFormat format = new DecimalFormat(FIRST_COLUMN_INDEX); //$NON-NLS-1$
        return step -> {
            final Action firstAction = step.getActions().get(0);
            final Map<String, String> parameters = firstAction.getParameters();
            if (parameters.get(ImplicitParameters.COLUMN_ID.getKey()) == null) {
                // this action is not applied on a column so no need to do a shift
                return step;
            }
            final int columnId = Integer.parseInt(parameters.get(ImplicitParameters.COLUMN_ID.getKey()));
            if (columnId > shiftColumnAfterId) {
                parameters.put(ImplicitParameters.COLUMN_ID.getKey(), format.format(columnId + (long) shiftNumber)); // $NON-NLS-1$
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
     * @param head        The head step
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
     * @param preparation  The preparation
     * @param startStepId  The step id to start the (re)write. The following steps will be erased
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
     * @param appendStep  The appendStep to apply.
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

        // Update preparation head step
        preparation.setHeadId(newHead.id());
        preparation.setLastModificationDate(System.currentTimeMillis());

        preparationRepository.add(newContent);
        preparationRepository.add(newHead);
        preparationRepository.add(preparation);
    }

    /**
     * Deletes the step of specified id of the specified preparation
     *
     * @param preparation    the specified preparation
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
     * @param preparation  the preparation containing the step to move
     * @param stepId       the id of the step to move
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
                    build().put(ID, preparation.getId()).put(STEP_ID, stepId));
        }
        if (parentIndex < 0) {
            throw new TDPException(PREPARATION_STEP_DOES_NOT_EXIST,
                    build().put(ID, preparation.getId()).put(STEP_ID, parentStepId));
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
            if (!reorderStepsUtils.isStepOrderInvalid(allAppendSteps)) {
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

        invalidatePreparationStep(step);

        // ...and create new one for step
        final StepRowMetadata stepRowMetadata = new StepRowMetadata(rowMetadata);
        preparationRepository.add(stepRowMetadata);
        step.setRowMetadata(stepRowMetadata.id());
        preparationRepository.add(step);
    }

    public void invalidatePreparationStep(String stepId) {
        final PersistentStep step = preparationRepository.get(stepId, PersistentStep.class);
        invalidatePreparationStep(step);
    }

    private void invalidatePreparationStep(PersistentStep step) {
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
