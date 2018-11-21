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

package org.talend.dataprep.api.service;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.talend.dataprep.command.CommandHelper.toStream;
import static org.talend.dataprep.exception.error.APIErrorCodes.INVALID_HEAD_STEP_USING_DELETED_DATASET;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_STEP_DOES_NOT_EXIST;
import static org.talend.dataprep.i18n.DataprepBundle.message;
import static org.talend.dataprep.util.SortAndOrderHelper.Order;
import static org.talend.dataprep.util.SortAndOrderHelper.Sort;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.PreparationAddAction;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.preparation.PreparationDetailsDTO;
import org.talend.dataprep.api.preparation.PreparationListItemDTO;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.api.PreviewAddParameters;
import org.talend.dataprep.api.service.api.PreviewDiffParameters;
import org.talend.dataprep.api.service.api.PreviewUpdateParameters;
import org.talend.dataprep.api.service.command.preparation.CachePreparationEviction;
import org.talend.dataprep.api.service.command.preparation.DiffMetadata;
import org.talend.dataprep.api.service.command.preparation.FindStep;
import org.talend.dataprep.api.service.command.preparation.PreparationCopy;
import org.talend.dataprep.api.service.command.preparation.PreparationCopyStepsFrom;
import org.talend.dataprep.api.service.command.preparation.PreparationCreate;
import org.talend.dataprep.api.service.command.preparation.PreparationDelete;
import org.talend.dataprep.api.service.command.preparation.PreparationDeleteAction;
import org.talend.dataprep.api.service.command.preparation.PreparationGetContent;
import org.talend.dataprep.api.service.command.preparation.PreparationGetMetadata;
import org.talend.dataprep.api.service.command.preparation.PreparationList;
import org.talend.dataprep.api.service.command.preparation.PreparationLock;
import org.talend.dataprep.api.service.command.preparation.PreparationMove;
import org.talend.dataprep.api.service.command.preparation.PreparationMoveHead;
import org.talend.dataprep.api.service.command.preparation.PreparationReorderStep;
import org.talend.dataprep.api.service.command.preparation.PreparationUnlock;
import org.talend.dataprep.api.service.command.preparation.PreparationUpdateAction;
import org.talend.dataprep.api.service.command.preparation.PreviewAdd;
import org.talend.dataprep.api.service.command.preparation.PreviewDiff;
import org.talend.dataprep.api.service.command.preparation.PreviewUpdate;
import org.talend.dataprep.api.service.command.transformation.GetPreparationColumnTypes;
import org.talend.dataprep.command.CommandHelper;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.command.preparation.PreparationGetActions;
import org.talend.dataprep.command.preparation.PreparationSummaryGet;
import org.talend.dataprep.command.preparation.PreparationUpdate;
import org.talend.dataprep.dataset.adapter.DatasetClient;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.PublicAPI;
import org.talend.dataprep.transformation.actions.datablending.Lookup;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.util.InjectorUtil;

import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
public class PreparationAPI extends APIService {

    @Autowired
    private DataSetAPI dataSetAPI;

    @Autowired
    private DatasetClient datasetClient;

    @Autowired
    private ActionRegistry registry;

    @Autowired
    private InjectorUtil injectorUtil;

    @RequestMapping(value = "/api/preparations", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all preparations.",
            notes = "Returns the list of preparations the current user is allowed to see.")
    @Timed
    public Stream<PreparationListItemDTO> listPreparations(
            @ApiParam(name = "name",
                    value = "Filter preparations by name.") @RequestParam(required = false) String name,
            @ApiParam(name = "folder_path", value = "Filter preparations by its folder path.") @RequestParam(
                    required = false, name = "folder_path") String folderPath,
            @ApiParam(name = "path",
                    value = "Filter preparations by full path. Should always return one preparation") @RequestParam(
                            required = false, name = "path") String path,
            @ApiParam(value = "Sort key, defaults to 'modification'.") @RequestParam(
                    defaultValue = "lastModificationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(
                    defaultValue = "desc") Order order) {
        GenericCommand<InputStream> command = getCommand(PreparationList.class, name, folderPath, path, sort, order);
        return toStream(PreparationDTO.class, mapper, command)//
                .map(dto -> beanConversionService.convert(dto, PreparationListItemDTO.class,
                        APIService::injectDataSetName));
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a new preparation for preparation content in body.", notes = "Returns the created preparation id.")
    @Timed
    public String createPreparation(
            @ApiParam(name = "folder", value = "Where to store the preparation.") @RequestParam(value = "folder") String folder,
            @ApiParam(name = "body", value = "The original preparation. You may set all values, service will override values you can't write to.") @RequestBody Preparation preparation) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating a preparation in {} (pool: {} )...", folder, getConnectionStats());
        }

        final DataSetMetadata dataSetMetadata = datasetClient.getDataSetMetadata(preparation.getDataSetId());
        if (StringUtils.isEmpty(preparation.getName())) {
            preparation.setName((dataSetMetadata.getName() != null ? dataSetMetadata.getName() + " " : "")
                    + message("preparation.create.suffix"));
        }
        final RowMetadata rowMetadata = dataSetMetadata.getRowMetadata();
        preparation.setRowMetadata(rowMetadata);

        PreparationCreate preparationCreate = getCommand(PreparationCreate.class, preparation, folder);
        final String preparationId = preparationCreate.execute();

        LOG.info("New Preparation #{}, name: {}, created in folder {}", preparationId, preparation.getName(), folder);

        return preparationId;
    }

    @RequestMapping(value = "/api/preparations/{id}", method = PUT, consumes = APPLICATION_JSON_VALUE,
            produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Update a preparation with content in body.", notes = "Returns the updated preparation id.")
    @Timed
    public String updatePreparation(
            @ApiParam(name = "id", value = "The id of the preparation to update.") @PathVariable("id") String id,
            @ApiParam(name = "body",
                    value = "The updated preparation. Null values are ignored during update. You may set all values, service will override values you can't write to.") @RequestBody PreparationDTO preparation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating preparation (pool: {} )...", getConnectionStats());
        }
        PreparationUpdate preparationUpdate = getCommand(PreparationUpdate.class, id, preparation);
        final String preparationId = preparationUpdate.execute();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updated preparation (pool: {} )...", getConnectionStats());
        }
        return preparationId;
    }

    @RequestMapping(value = "/api/preparations/{id}", method = DELETE, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Delete a preparation by id",
            notes = "Delete a preparation content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing preparation id returns empty content.")
    @Timed
    public String deletePreparation(
            @ApiParam(name = "id", value = "The id of the preparation to delete.") @PathVariable("id") String id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting preparation (pool: {} )...", getConnectionStats());
        }
        final CachePreparationEviction evictPreparationCache = getCommand(CachePreparationEviction.class, id);
        final PreparationDelete preparationDelete = getCommand(PreparationDelete.class, id, evictPreparationCache);
        final String preparationId = preparationDelete.execute();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleted preparation (pool: {} )...", getConnectionStats());
        }
        return preparationId;
    }

    /**
     * Copy a preparation from the given id
     *
     * @param id the preparation id to copy
     * @param destination where to copy the preparation to.
     * @param newName optional new name for the preparation.
     * @return The copied preparation id.
     */
    //@formatter:off
    @RequestMapping(value = "/api/preparations/{id}/copy", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Copy a preparation", produces = TEXT_PLAIN_VALUE, notes = "Copy a preparation based the provided id.")
    public String copy(
            @ApiParam(value = "Id of the preparation to copy") @PathVariable(value = "id") String id,
            @ApiParam(value = "Optional new name of the copied preparation, if not set the copy will get the original name.") @RequestParam(required = false) String newName,
            @ApiParam(value = "The destination path to create the entry.") @RequestParam(required = false) String destination) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Copying preparation {} to '{}' with new name '{}' (pool: {} )...", id, destination, newName,
                    getConnectionStats());
        }

        HystrixCommand<String> copy = getCommand(PreparationCopy.class, id, destination, newName);
        String copyId = copy.execute();

        LOG.info("Preparation {} copied to {}/{} done --> {}", id, destination, newName, copyId);

        return copyId;
    }

    /**
     * Move a preparation to another folder.
     *
     * @param id the preparation id to move.
     * @param folder where to find the preparation.
     * @param destination where to move the preparation.
     * @param newName optional new preparation name.
     */
    //@formatter:off
    @RequestMapping(value = "/api/preparations/{id}/move", method = PUT)
    @ApiOperation(value = "Move a Preparation", notes = "Move a preparation to another folder.")
    @Timed
    public void move(@PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the preparation to move") String id,
                     @ApiParam(value = "The original folder path of the preparation.") @RequestParam(defaultValue = "", required = false) String folder,
                     @ApiParam(value = "The new folder path of the preparation.") @RequestParam() String destination,
                     @ApiParam(value = "The new name of the moved dataset.") @RequestParam(defaultValue = "", required = false) String newName) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Moving preparation (pool: {} )...", getConnectionStats());
        }

        HystrixCommand<Void> move = getCommand(PreparationMove.class, id, folder, destination, newName);
        move.execute();

        LOG.info("Preparation {} moved from {} to {}/'{}'", id, folder, destination, newName);
    }

    @RequestMapping(value = "/api/preparations/{id}/details", method = RequestMethod.GET,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preparation by id and details.", notes = "Returns the preparation details.")
    @Timed
    public PreparationDetailsDTO getPreparation(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") String preparationId, //
            @RequestParam(value = "stepId", defaultValue = "head") @ApiParam(name = "stepId",
                    value = "optional step id", defaultValue = "head") String stepId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving preparation details (pool: {} )...", getConnectionStats());
        }

        try {
            return getCommand(PreparationDetailsGet.class, preparationId, stepId).execute();
        } catch (Exception e) {
            LOG.error("Unable to get preparation {}", preparationId, e);
            throw new TDPException(APIErrorCodes.UNABLE_TO_GET_PREPARATION_DETAILS, e);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved preparation details (pool: {} )...", getConnectionStats());
            }
            LOG.info("Preparation {} retrieved", preparationId);
        }
    }

    @RequestMapping(value = "/api/preparations/{id}/summary", method = RequestMethod.GET,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preparation by id and details.", notes = "Returns the preparation details.")
    @Timed
    public PreparationDTO getPreparationSummary(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") String preparationId, //
            @RequestParam(value = "stepId", defaultValue = "head") @ApiParam(name = "stepId",
                    value = "optional step id", defaultValue = "head") String stepId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving preparation summary (pool: {} )...", getConnectionStats());
        }

        try {
            final PreparationSummaryGet enrichPreparation =
                    getCommand(PreparationSummaryGet.class, preparationId, stepId);
            return enrichPreparation.execute();
        } catch (Exception e) {
            LOG.error("Unable to get preparation {}", preparationId, e);
            throw new TDPException(APIErrorCodes.UNABLE_TO_GET_PREPARATION_DETAILS, e);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved preparation summary (pool: {} )...", getConnectionStats());
            }
            LOG.info("Preparation {} retrieved", preparationId);
        }
    }

    @RequestMapping(value = "/api/preparations/{id}/content", method = RequestMethod.GET,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get preparation content by id and at a given version.",
            notes = "Returns the preparation content at version.")
    @Timed
    public ResponseEntity<StreamingResponseBody> getPreparation( //
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") String preparationId, //
            @RequestParam(value = "version", defaultValue = "head") @ApiParam(name = "version",
                    value = "Version of the preparation (can be 'origin', 'head' or the version id). Defaults to 'head'.") String version,
            @RequestParam(value = "from", defaultValue = "HEAD") @ApiParam(name = "from",
                    value = "Where to get the data from") ExportParameters.SourceType from,
            @RequestParam(value = "filter", required = false) @ApiParam(name = "filter",
                    value = "A filter apply on the content") String filter) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving preparation content for {}/{} (pool: {} )...", preparationId, version,
                    getConnectionStats());
        }

        try {
            GenericCommand<InputStream> command =
                    getCommand(PreparationGetContent.class, preparationId, version, from, filter);
            HttpResponseContext.contentType(APPLICATION_JSON_VALUE);
            return CommandHelper.toStreaming(command);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved preparation content (pool: {} )...", getConnectionStats());
            }
        }
    }

    @RequestMapping(value = "/api/preparations/{id}/metadata", method = RequestMethod.GET,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get preparation metadata by id and at a given version.",
            notes = "Returns the preparation metadata at version.")
    @Timed
    public ResponseEntity<DataSetMetadata> getPreparationMetadata( //
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") String preparationId, //
            @RequestParam(value = "version", defaultValue = "head") @ApiParam(name = "version",
                    value = "Version of the preparation (can be 'origin', 'head' or the version id). Defaults to 'head'.") String version) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving preparation metadata for {}/{} (pool: {} )...", preparationId, version,
                    getConnectionStats());
        }

        try {
            return getCommand(PreparationGetMetadata.class, preparationId, version).execute();
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved preparation metadata (pool: {} )...", getConnectionStats());
            }
        }
    }

    // TODO: this API should take a list of AppendStep.
    @RequestMapping(value = "/api/preparations/{id}/actions", method = POST, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Adds an action at the end of preparation.",
            notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void addPreparationAction(
            @ApiParam(name = "id", value = "Preparation id.") @PathVariable(value = "id") final String preparationId,
            @ApiParam("Action to add at end of the preparation.") @RequestBody final AppendStep actionsContainer) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding action to preparation (pool: {} )...", getConnectionStats());
        }

        // This trick is to keep the API taking and unrolling ONE AppendStep until the codefreeze but this must not stay
        // that way
        List<AppendStep> stepsToAppend = actionsContainer.getActions().stream().map(a -> {
            AppendStep s = new AppendStep();
            s.setActions(singletonList(a));
            return s;
        }).collect(toList());

        getCommand(PreparationAddAction.class, preparationId, stepsToAppend).execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Added action to preparation (pool: {} )...", getConnectionStats());
        }

    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/{preparationId}/actions/{stepId}", method = PUT, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Updates an action in the preparation.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void updatePreparationAction(@ApiParam(name = "preparationId", value = "Preparation id.") @PathVariable(value = "preparationId") final String preparationId,
                                        @ApiParam(name = "stepId", value = "Step id in the preparation.") @PathVariable(value = "stepId") final String stepId,
                                        @ApiParam("New content for the action.") @RequestBody final AppendStep step) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating preparation action at step #{} (pool: {} )...", stepId, getConnectionStats());
        }

        // get the preparation
        PreparationDTO preparation = internalGetPreparation(preparationId);

        // get the preparation actions for up to the updated action
        final int stepIndex = new ArrayList<>(preparation.getSteps()).indexOf(stepId);
        final String parentStepId = preparation.getSteps().get(stepIndex - 1);
        final PreparationGetActions getActionsCommand =
                getCommand(PreparationGetActions.class, preparationId, parentStepId);

        // get the diff
        final DiffMetadata diffCommand = getCommand(DiffMetadata.class, preparation.getDataSetId(), preparationId,
                step.getActions(), getActionsCommand);

        // get the update action command and execute it
        final HystrixCommand<Void> command =
                getCommand(PreparationUpdateAction.class, preparationId, stepId, step, diffCommand);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updated preparation action at step #{} (pool: {} )...", stepId, getConnectionStats());
        }
    }

    @RequestMapping(value = "/api/preparations/{id}/actions/{stepId}", method = DELETE,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete an action in the preparation.",
            notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void deletePreparationAction(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") final String preparationId,
            @PathVariable(value = "stepId") @ApiParam(name = "stepId",
                    value = "Step id to delete.") final String stepId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting preparation action at step #{} (pool: {} ) ...", stepId, //
                    getConnectionStats());
        }

        final HystrixCommand<Void> command = getCommand(PreparationDeleteAction.class, preparationId, stepId);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleted preparation action at step #{} (pool: {} ) ...", stepId, //
                    getConnectionStats());
        }
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/{id}/head/{headId}", method = PUT)
    @ApiOperation(value = "Changes the head of the preparation.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void setPreparationHead(@PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") final String preparationId,
                                   @PathVariable(value = "headId") @ApiParam(name = "headId", value = "New head step id") final String headId) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Moving preparation #{} head to step '{}'...", preparationId, headId);
        }

        Step step = getCommand(FindStep.class, headId).execute();
        if (step == null) {
            throw new TDPException(PREPARATION_STEP_DOES_NOT_EXIST);
        } else if (isHeadStepDependingOnDeletedDataSet(preparationId, step.id())) {
            final HystrixCommand<Void> command = getCommand(PreparationMoveHead.class, preparationId, headId);
            command.execute();
        } else {
            throw new TDPException(INVALID_HEAD_STEP_USING_DELETED_DATASET);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Moved preparation #{} head to step '{}'...", preparationId, headId);
        }
    }

    @RequestMapping(value = "/api/preparations/{preparationId}/lock", method = PUT, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Mark a preparation as locked by a user.",
            notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void lockPreparation(@PathVariable(value = "preparationId") @ApiParam(name = "preparationId",
            value = "Preparation id.") final String preparationId) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Locking preparation #{}...", preparationId);
        }

        final HystrixCommand<Void> command = getCommand(PreparationLock.class, preparationId);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Locked preparation #{}...", preparationId);
        }
    }

    @RequestMapping(value = "/api/preparations/{preparationId}/unlock", method = PUT, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Mark a preparation as unlocked by a user.",
            notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void unlockPreparation(@PathVariable(value = "preparationId") @ApiParam(name = "preparationId",
            value = "Preparation id.") final String preparationId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Locking preparation #{}...", preparationId);
        }

        final HystrixCommand<Void> command = getCommand(PreparationUnlock.class, preparationId);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Locked preparation #{}...", preparationId);
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
    //@formatter:off
    @RequestMapping(value = "/api/preparations/{id}/steps/copy", method = PUT)
    @ApiOperation(value = "Copy the steps from another preparation", notes = "Copy the steps from another preparation if this one has no steps.")
    @Timed
    public void copyStepsFrom(@ApiParam(value="the preparation id to update") @PathVariable("id")String id,
                              @ApiParam(value = "the preparation to copy the steps from.") @RequestParam String from) {
    //@formatter:on

        LOG.debug("copy preparations steps from {} to {}", from, id);

        final HystrixCommand<Void> command = getCommand(PreparationCopyStepsFrom.class, id, from);
        command.execute();

        LOG.info("preparation's steps copied from {} to {}", from, id);
    }

    /**
     * Moves the step of specified id <i>stepId</i> after step of specified id <i>parentId</i> within the specified
     * preparation.
     *
     * @param preparationId the Id of the specified preparation
     * @param stepId the Id of the specified step to move
     * @param parentStepId the Id of the specified step which will become the parent of the step to move
     */
    // formatter:off
    @RequestMapping(value = "/api/preparations/{preparationId}/steps/{stepId}/order", method = POST,
            consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Moves a step within a preparation just after the specified <i>parentStepId</i>",
            notes = "Moves a step within a preparation.")
    @Timed
    public void moveStep(@PathVariable("preparationId") final String preparationId,
            @ApiParam(value = "The current index of the action we want to move.") @PathVariable("stepId") String stepId,
            @ApiParam(value = "The current index of the action we want to move.") @RequestParam String parentStepId) {
        //@formatter:on

        LOG.info("Moving step {} after step {}, within preparation {}", stepId, parentStepId, preparationId);

        final HystrixCommand<String> command =
                getCommand(PreparationReorderStep.class, preparationId, stepId, parentStepId);
        command.execute();

        LOG.debug("Step {} moved after step {}, within preparation {}", stepId, parentStepId, preparationId);

    }

    // ---------------------------------------------------------------------------------
    // ----------------------------------------PREVIEW----------------------------------
    // ---------------------------------------------------------------------------------

    //@formatter:off
    @RequestMapping(value = "/api/preparations/preview/diff", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preview diff between 2 steps of the same preparation.")
    @Timed
    public StreamingResponseBody previewDiff(@RequestBody final PreviewDiffParameters input) {
    //@formatter:on

        // get preparation details
        final PreparationDTO preparation = internalGetPreparation(input.getPreparationId());
        final List<Action> lastActiveStepActions = internalGetActions(preparation.getId(), input.getCurrentStepId());
        final List<Action> previewStepActions = internalGetActions(preparation.getId(), input.getPreviewStepId());

        final HystrixCommand<InputStream> transformation =
                getCommand(PreviewDiff.class, input, preparation, lastActiveStepActions, previewStepActions);
        return executePreviewCommand(transformation);
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/preview/update", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preview diff between the same step of the same preparation but with one step update.")
    public StreamingResponseBody previewUpdate(@RequestBody final PreviewUpdateParameters input) {
    //@formatter:on

        // get preparation details
        final PreparationDTO preparation = internalGetPreparation(input.getPreparationId());
        final List<Action> actions = internalGetActions(preparation.getId());

        final HystrixCommand<InputStream> transformation = getCommand(PreviewUpdate.class, input, preparation, actions);
        return executePreviewCommand(transformation);
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/preview/add", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preview between the head step and a new appended transformation")
    public StreamingResponseBody previewAdd(@RequestBody @Valid final PreviewAddParameters input) {
    //@formatter:on

        PreparationDTO preparation = null;
        List<Action> actions = new ArrayList<>(0);

        // get preparation details with dealing with preparations
        if (StringUtils.isNotBlank(input.getPreparationId())) {
            preparation = internalGetPreparation(input.getPreparationId());
            actions = internalGetActions(preparation.getId());
        }

        final HystrixCommand<InputStream> transformation = getCommand(PreviewAdd.class, input, preparation, actions);
        return executePreviewCommand(transformation);
    }

    private StreamingResponseBody executePreviewCommand(HystrixCommand<InputStream> transformation) {
        return CommandHelper.toStreaming(transformation);
    }

    /**
     * Return the semantic types for a given preparation / column.
     *
     * @param preparationId the preparation id.
     * @param columnId the column id.
     * @param stepId the step id (optional, if not specified, it's 'head')
     * @return the semantic types for a given preparation / column.
     */
    @RequestMapping(value = "/api/preparations/{preparationId}/columns/{columnId}/types", method = GET,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "list the types of the wanted column",
            notes = "This list can be used by user to change the column type.")
    @Timed
    @PublicAPI
    public ResponseEntity<StreamingResponseBody> getPreparationColumnSemanticCategories(
            @ApiParam(value = "The preparation id") @PathVariable String preparationId,
            @ApiParam(value = "The column id") @PathVariable String columnId,
            @ApiParam(value = "The preparation version") @RequestParam(defaultValue = "head") String stepId) {

        LOG.debug("listing semantic types for preparation {} / {}, column {}", preparationId, columnId, stepId);
        return CommandHelper.toStreaming(getCommand(GetPreparationColumnTypes.class, preparationId, columnId, stepId));
    }

    /**
     * Helper method used to retrieve preparation actions via a hystrix command.
     *
     * @param preparationId the preparation id to get the actions from.
     * @return the preparation actions.
     */
    private List<Action> internalGetActions(String preparationId) {
        return internalGetActions(preparationId, "head");
    }

    /**
     * Helper method used to retrieve preparation actions via a hystrix command.
     *
     * @param preparationId the preparation id to get the actions from.
     * @param stepId the preparation version.
     * @return the preparation actions.
     */
    private List<Action> internalGetActions(String preparationId, String stepId) {
        final PreparationGetActions getActionsCommand = getCommand(PreparationGetActions.class, preparationId, stepId);
        return getActionsCommand.execute();
    }

    /**
     * Helper method used to get a preparation for internal class use.
     *
     * @param preparationId the preparation id.
     * @return the preparation.
     */
    private PreparationDTO internalGetPreparation(String preparationId) {
        GenericCommand<PreparationDTO> command = getCommand(PreparationSummaryGet.class, preparationId);
        return command.execute();
    }

    private boolean isHeadStepDependingOnDeletedDataSet(String preparationId, String stepId) {
        List<Action> actions = internalGetActions(preparationId, stepId);
        boolean oneActionRefersToNonexistentDataset = actions
                .stream() //
                .filter(action -> StringUtils.equals(action.getName(), Lookup.LOOKUP_ACTION_NAME)) //
                .map(action -> action.getParameters().get(Lookup.Parameters.LOOKUP_DS_ID.getKey())) //
                .anyMatch(dsId -> {
                    boolean hasNoDataset;
                    try {
                        hasNoDataset = dataSetAPI.getMetadata(dsId) == null;
                    } catch (TDPException e) {
                        // Dataset could not be retrieved => Main reason is not present
                        LOG.debug("The data set could not be retrieved: " + e);
                        hasNoDataset = true;
                    }
                    return hasNoDataset;
                });
        return !oneActionRefersToNonexistentDataset;
    }
}
