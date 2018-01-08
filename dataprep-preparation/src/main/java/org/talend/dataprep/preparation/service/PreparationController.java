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

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.*;
import org.talend.dataprep.exception.json.JsonErrorCodeDescription;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.util.SortAndOrderHelper.Order;
import org.talend.dataprep.util.SortAndOrderHelper.Sort;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(value = "preparations", basePath = "/preparations", description = "Operations on preparations")
public class PreparationController {

    private static final Logger LOGGER = getLogger(PreparationController.class);

    @Autowired
    private PreparationService preparationService;

    /**
     * Create a preparation from the http request body.
     *
     * @param preparation the preparation to create.
     * @param folderId      where to store the preparation.
     * @return the created preparation id.
     */
    @RequestMapping(value = "/preparations", method = POST)
    @ApiOperation(value = "Create a preparation", notes = "Returns the id of the created preparation.")
    @Timed
    public String create(@ApiParam("preparation") @RequestBody @Valid final Preparation preparation,
                         @ApiParam(value = "The folderId path to create the entry.") @RequestParam String folderId) {
        return preparationService.create(preparation, folderId);
    }

    /**
     * List all the preparations id.
     *
     * @param sort  how the preparation should be sorted (default is 'last modification date').
     * @param order how to apply the sort.
     * @return the preparations id list.
     */
    @RequestMapping(value = "/preparations", method = GET)
    @ApiOperation(value = "List all preparations id", notes = "Returns the list of preparations ids the current user is allowed to see. Creation date is always displayed in UTC time zone. See 'preparations/all' to get all details at once.")
    @Timed
    public Stream<String> list(
            @ApiParam(name = "name", value = "Filter preparations by name.") @RequestParam(required = false) String name,
            @ApiParam(name = "folder_path", value = "Filter preparations by folder path.") @RequestParam(required = false, name = "folder_path") String folderPath,
            @ApiParam(name = "path", value = "Filter preparations by full path (<folder path>/<preparation name>).") @RequestParam(required = false, name = "path") String path,
            @ApiParam(value = "Sort key (by name or date).") @RequestParam(defaultValue = "lastModificationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc).") @RequestParam(defaultValue = "desc") Order order) {
        LOGGER.debug("Get list of preparations (summary).");
        return preparationService.listAll(name, folderPath, path, sort, order).map(Preparation::id);
    }

    /**
     * List all preparation details.
     *
     * @param sort  how to sort the preparations.
     * @param order how to order the sort.
     * @return the preparation details.
     */
    @RequestMapping(value = "/preparations/details", method = GET)
    @ApiOperation(value = "List all preparations", notes = "Returns the list of preparations details the current user is allowed to see. Creation date is always displayed in UTC time zone. This operation return all details on the preparations.")
    @Timed
    public Stream<UserPreparation> listAll(
            @ApiParam(name = "name", value = "Filter preparations by name.") @RequestParam(required = false) String name,
            @ApiParam(name = "folder_path", value = "Filter preparations by folder path.") @RequestParam(required = false, name = "folder_path") String folderPath,
            @ApiParam(name = "path", value = "Filter preparations by full path (<folder path>/<preparation name>).") @RequestParam(required = false, name = "path") String path,
            @ApiParam(value = "Sort key (by name or date).") @RequestParam(defaultValue = "lastModificationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc).") @RequestParam(defaultValue = "desc") Order order) {
        return preparationService.listAll(name, folderPath, path, sort, order);
    }

    /**
     * List all preparation summaries.
     *
     * @return the preparation summaries, sorted by descending last modification date.
     */
    @RequestMapping(value = "/preparations/summaries", method = GET)
    @ApiOperation(value = "List all preparations", notes = "Returns the list of preparations summaries the current user is allowed to see. Creation date is always displayed in UTC time zone.")
    @Timed
    public Stream<PreparationSummary> listSummary(
            @ApiParam(name = "name", value = "Filter preparations by name.") @RequestParam(required = false) String name,
            @ApiParam(name = "folder_path", value = "Filter preparations by folder path.") @RequestParam(required = false, name = "folder_path") String folderPath,
            @ApiParam(name = "path", value = "Filter preparations by full path (<folder path>/<preparation name>).") @RequestParam(required = false, name = "path") String path,
            @ApiParam(value = "Sort key (by name or date).") @RequestParam(defaultValue = "lastModificationDate") Sort sort,
            @ApiParam(value = "Order for sort key (desc or asc).") @RequestParam(defaultValue = "desc") Order order) {
        LOGGER.debug("Get list of preparations (summary).");
        return preparationService.listSummary(name, folderPath, path, sort, order);
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
     * @param sort       Sort key (by name, creation date or modification date).
     * @param order      Order for sort key (desc or asc).
     */
    @RequestMapping(value = "/preparations/search", method = GET)
    @ApiOperation(value = "Search for preparations details", notes = "Returns the list of preparations details that match the search criteria.")
    @Timed
    public Stream<UserPreparation> searchPreparations(
            @RequestParam(required = false) @ApiParam("dataSetId") String dataSetId,
            @RequestParam(required = false) @ApiParam(value = "Id of the folder where to look for preparations") String folderId,
            @RequestParam(required = false) @ApiParam(value = "Path of the folder where to look for preparations") String folderPath,
            @RequestParam(required = false) @ApiParam("name") String name,
            @RequestParam(defaultValue = "true") @ApiParam("exactMatch") boolean exactMatch,
            @RequestParam(defaultValue = "lastModificationDate") @ApiParam(value = "Sort key (by name or date).") Sort sort,
            @RequestParam(defaultValue = "desc") @ApiParam(value = "Order for sort key (desc or asc).") Order order) {

        return preparationService.searchPreparations(dataSetId, folderId, name, exactMatch, folderPath, sort, order);
    }

    /**
     * Copy the given preparation to the given name / folder ans returns the new if in the response.
     *
     * @param name        the name of the copied preparation, if empty, the name is "orginal-preparation-name Copy"
     * @param destination the folder path where to copy the preparation, if empty, the copy is in the same folder.
     * @return The new preparation id.
     */
    @RequestMapping(value = "/preparations/{id}/copy", method = POST, produces = TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Copy a preparation", produces = TEXT_PLAIN_VALUE, notes = "Copy the preparation to the new name / folder and returns the new id.")
    @Timed
    public String copy(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the preparation to copy") String preparationId,
            @ApiParam(value = "The name of the copied preparation.") @RequestParam(required = false) String name,
            @ApiParam(value = "The folder path to create the copy.") @RequestParam() String destination) throws IOException {
        return preparationService.copy(preparationId, name, destination);
    }

    /**
     * Move a preparation to an other folder.
     *
     * @param folder      The original folder of the preparation.
     * @param destination The new folder of the preparation.
     * @param newName     The new preparation name.
     */
    @RequestMapping(value = "/preparations/{id}/move", method = PUT)
    @ApiOperation(value = "Move a preparation", notes = "Move a preparation to an other folder.")
    @Timed
    public void move(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the preparation to move") String preparationId,
            @ApiParam(value = "The original folder path of the preparation.") @RequestParam String folder,
            @ApiParam(value = "The new folder path of the preparation.") @RequestParam String destination,
            @ApiParam(value = "The new name of the moved dataset.") @RequestParam(defaultValue = "") String newName)
            throws IOException {
        preparationService.move(preparationId, folder, destination, newName);
    }

    /**
     * Delete the preparation that match the given id.
     *
     * @param id the preparation id to delete.
     */
    @RequestMapping(value = "/preparations/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete a preparation by id", notes = "Delete a preparation content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing preparation id returns empty content.")
    @Timed
    public void delete(@PathVariable(value = "id") @ApiParam(name = "id", value = "Id of the preparation to delete") String id) {
        preparationService.delete(id);
    }

    /**
     * Update a preparation.
     *
     * @param id          the preparation id to update.
     * @param preparation the updated preparation.
     * @return the updated preparation id.
     */
    @RequestMapping(value = "/preparations/{id}", method = PUT)
    @ApiOperation(value = "Create a preparation", notes = "Returns the id of the updated preparation.")
    @Timed
    public String update(@ApiParam("id") @PathVariable("id") String id,
                         @RequestBody @ApiParam("preparation") final PreparationMessage preparation) {
        return preparationService.update(id, preparation);
    }

    /**
     * Update a preparation steps.
     *
     * @param stepId the step to update.
     * @param rowMetadata the row metadata to associate with step.
     * @return the updated step id.
     */
    @RequestMapping(value = "/preparations/steps/{stepId}/metadata", method = PUT, produces = TEXT_PLAIN_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update a preparation steps", notes = "Returns the id of the updated step.")
    @Timed
    public String updateStepMetadata(@ApiParam("stepId") @PathVariable("stepId") String stepId,
                                     @RequestBody @ApiParam("rowMetadata") final RowMetadata rowMetadata) {
        preparationService.updatePreparationStep(stepId, rowMetadata);
        return stepId;
    }

    /**
     * Get a preparation step metadata.
     *
     * @param stepId the steps to get metadata from.
     * @return the row metadata associated with step.
     */
    @RequestMapping(value = "/preparations/steps/{stepId}/metadata", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update a preparation steps", notes = "Returns the id of the updated step.")
    @Timed
    public RowMetadata getStepMetadata(@ApiParam("stepId") @PathVariable("stepId") String stepId) {
        return preparationService.getPreparationStep(stepId);
    }

    /**
     * Copy the steps from the another preparation to this one.
     * <p>
     * This is only allowed if this preparation has no steps.
     *
     * @param id   the preparation id to update.
     * @param from the preparation id to copy the steps from.
     */
    @RequestMapping(value = "/preparations/{id}/steps/copy", method = PUT)
    @ApiOperation(value = "Copy the steps from another preparation", notes = "Copy the steps from another preparation if this one has no steps.")
    @Timed
    public void copyStepsFrom(@ApiParam(value = "the preparation id to update") @PathVariable("id") String id,
                              @ApiParam(value = "the preparation to copy the steps from.") @RequestParam String from) {
        preparationService.copyStepsFrom(id, from);
    }

    /**
     * Return a preparation details.
     *
     * @param id the wanted preparation id.
     * @param stepId optional step id.
     * @return the preparation details.
     */
    @RequestMapping(value = "/preparations/{id}/details", method = GET)
    @ApiOperation(value = "Get preparation details", notes = "Return the details of the preparation with provided id.")
    @Timed
    public PreparationMessage getDetails( //
            @ApiParam("id") @PathVariable("id") String id, //
            @ApiParam(value = "stepId", defaultValue = "head") @RequestParam(value = "stepId", defaultValue = "head") String stepId) {
        return preparationService.getPreparationDetails(id, stepId);
    }

    /**
     * Return a preparation.
     *
     * @param id the wanted preparation id.
     * @return the preparation details.
     */
    @RequestMapping(value = "/preparations/{id}", method = GET)
    @ApiOperation(value = "Get preparation", notes = "Return the preparation with provided id.")
    @Timed
    public Preparation get(@ApiParam("id") @PathVariable("id") String id) {
        return preparationService.getPreparation(id);
    }

    /**
     * Return the folder that holds this preparation.
     *
     * @param id the wanted preparation id.
     * @return the folder that holds this preparation.
     */
    @RequestMapping(value = "/preparations/{id}/folder", method = GET)
    @ApiOperation(value = "Get preparation details", notes = "Return the details of the preparation with provided id.")
    @Timed
    public Folder searchLocation(@ApiParam(value = "the preparation id") @PathVariable("id") String id) {
        return preparationService.searchLocation(id);
    }

    @RequestMapping(value = "/preparations/{id}/steps", method = GET)
    @ApiOperation(value = "Get all preparation steps id", notes = "Return the steps of the preparation with provided id.")
    @Timed
    public List<String> getSteps(@ApiParam("id") @PathVariable("id") String id) {
        return preparationService.getSteps(id);
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
    @RequestMapping(value = "/preparations/{id}/actions/{stepId}", method = PUT)
    @ApiOperation(value = "Updates an action in a preparation", notes = "Modifies an action in preparation's steps.")
    @Timed
    public void updateAction(@PathVariable("id") final String preparationId, @PathVariable("stepId") final String stepToModifyId,
                             @RequestBody final AppendStep newStep) {
        preparationService.updateAction(preparationId, stepToModifyId, newStep);
    }

    /**
     * Delete a step in a preparation.<br/>
     * STD : Step To Delete <br/>
     * <br/>
     * <ul>
     * <li>1. Extract the actions from STD (excluded) to the head. The actions list contains all the actions from the
     * STD's child to the head.</li>
     * <li>2. Filter the preparations that apply on a column created by the step to delete. Those steps will be removed
     * too.</li>
     * <li>2bis. Change the actions that apply on columns > STD last created column id. The created columns ids after
     * the STD are shifted.</li>
     * <li>3. Set preparation head to STD's parent, so STD will be excluded</li>
     * <li>4. Append each action after the new preparation head</li>
     * </ul>
     */
    @RequestMapping(value = "/preparations/{id}/actions/{stepId}", method = DELETE)
    @ApiOperation(value = "Delete an action in a preparation", notes = "Delete a step and all following steps from a preparation")
    @Timed
    public void deleteAction(@PathVariable("id") final String id, @PathVariable("stepId") final String stepToDeleteId) {
        preparationService.deleteAction(id, stepToDeleteId);
    }

    @RequestMapping(value = "/preparations/{id}/head/{headId}", method = PUT)
    @ApiOperation(value = "Move preparation head", notes = "Set head to the specified head id")
    @Timed
    public void setPreparationHead(@PathVariable("id") final String preparationId, //
                                   @PathVariable("headId") final String headId) {
        preparationService.setPreparationHead(preparationId, headId);
    }

    /**
     * Get all the actions of a preparation at given version.
     *
     * @param id      the wanted preparation id.
     * @param version the wanted preparation version.
     * @return the list of actions.
     */
    @RequestMapping(value = "/preparations/{id}/actions/{version}", method = GET)
    @ApiOperation(value = "Get all the actions of a preparation at given version.", notes = "Returns the action JSON at version.")
    @Timed
    public List<Action> getVersionedAction(@ApiParam("id") @PathVariable("id") final String id,
                                           @ApiParam("version") @PathVariable("version") final String version) {
        return preparationService.getVersionedAction(id, version);
    }

    /**
     * List all preparation related error codes.
     */
    @RequestMapping(value = "/preparations/errors", method = RequestMethod.GET)
    @ApiOperation(value = "Get all preparation related error codes.", notes = "Returns the list of all preparation related error codes.")
    @Timed
    public Iterable<JsonErrorCodeDescription> listErrors() {
        return preparationService.listErrors();
    }

    @RequestMapping(value = "/preparations/{preparationId}/lock", method = PUT)
    @ApiOperation(value = "Lock the specified preparation.", notes = "Returns a locked resource.")
    @Timed
    public void lockPreparation(@ApiParam("preparationId") @PathVariable("preparationId") final String preparationId) {
        preparationService.lockPreparation(preparationId);
    }

    @RequestMapping(value = "/preparations/{preparationId}/unlock", method = PUT)
    @ApiOperation(value = "Unlock the specified preparation.", notes = "Returns a locked resource.")
    @Timed
    public void unlockPreparation(@ApiParam("preparationId") @PathVariable("preparationId") final String preparationId) {
        preparationService.unlockPreparation(preparationId);
    }

    @RequestMapping(value = "/preparations/use/dataset/{datasetId}", method = HEAD)
    @ApiOperation(value = "Check if dataset is used by a preparation.", notes = "Returns no content, the response code is the meaning.")
    @Timed
    public ResponseEntity<Void> preparationsThatUseDataset(
            @ApiParam("datasetId") @PathVariable("datasetId") final String datasetId) {
        if (preparationService.isDatasetUsedInPreparation(datasetId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/preparations/{id}/steps/{stepId}/order", method = POST)
    @ApiOperation(value = "Moves a step within a preparation after a specified step",
                  notes = "Moves a step within a preparation after a specified step.")
    @Timed
    public void moveStep(@PathVariable("id") final String preparationId,
                         @ApiParam(value = "The id of the step we want to move.") @PathVariable String stepId,
                         @ApiParam(value = "The step that will become the parent of stepId") @RequestParam String parentStepId) {
        preparationService.moveStep(preparationId, stepId, parentStepId);
    }

    @RequestMapping(value = "/preparations/{id}/actions", method = POST)
    @ApiOperation(value = "Adds an action at the end of preparation.",
                  notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void addPreparationAction(
            @ApiParam(name = "id", value = "Preparation id.") @PathVariable(value = "id") final String preparationId,
            @ApiParam("Action to add at end of the preparation.") @RequestBody final List<AppendStep> steps) {
        for (AppendStep step : steps) {
            preparationService.addPreparationAction(preparationId, step);
        }
    }

    @RequestMapping(value = "/steps/{id}", method = GET)
    @ApiOperation(value = "Retrieve a specific step.",
                  notes = "Just find the step for this ID.")
    @Timed
    public Step getStep(@PathVariable("id") final String stepId) {
        return preparationService.getStep(stepId);
    }
}
