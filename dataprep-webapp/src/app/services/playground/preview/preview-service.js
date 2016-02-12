/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.playground.service:PreviewService
 * @description Preview service. This service holds the preview datagrid (SlickGrid) view
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.playground.service:DatagridService
 * @requires data-prep.services.preparation.service:PreparationService
 */
export default function PreviewService($q, state, DatagridService, PreparationService) {
    'ngInject';

    /**
     * @ngdoc property
     * @name reverter
     * @propertyOf data-prep.services.playground.service:PreviewService
     * @description [PRIVATE] The revert executor to apply on datagrid to go from current preview to original data
     */
    var reverter;

    /**
     * @ngdoc property
     * @name originalData
     * @propertyOf data-prep.services.playground.service:PreviewService
     * @description [PRIVATE] The original data (columns and records) before switching to preview
     */
    var originalData;

    /**
     * @ngdoc property
     * @name displayedTdpIds
     * @propertyOf data-prep.services.playground.service:PreviewService
     * @description [PRIVATE] The list of records TDP indexes that is displayed in the viewport
     */
    var displayedTdpIds;

    /**
     * @ngdoc property
     * @name previewCanceler
     * @propertyOf data-prep.services.playground.service:PreviewService
     * @description [PRIVATE] The preview cancel promise. When it is resolved, the pending request is rejected
     */
    var previewCanceler;

    var service = {
        /**
         * @ngdoc property
         * @name gridRangeIndex
         * @propertyOf data-prep.services.playground.service:PreviewService
         * @description The grid displayed rows id. It take filters into account.
         * This is updated by the {@link data-prep.datagrid.directive:Datagrid Datagrid} directive on scroll
         */
        gridRangeIndex: null,

        getPreviewDiffRecords: getPreviewDiffRecords,
        getPreviewUpdateRecords: getPreviewUpdateRecords,
        getPreviewAddRecords: getPreviewAddRecords,

        previewInProgress: previewInProgress,
        stopPendingPreview: stopPendingPreview,
        reset: reset,
        cancelPreview: cancelPreview
    };
    return service;

    /**
     * @ngdoc method
     * @name getDisplayedTdpIds
     * @methodOf data-prep.services.playground.service:PreviewService
     * @description [PRIVATE] Get the rows TDP ids in the range
     * @returns {object[]} The rows TDP ids in the range
     */
    function getDisplayedTdpIds() {
        var indexes = _.range(service.gridRangeIndex.top, service.gridRangeIndex.bottom + 1);
        return _.chain(indexes)
            .map(state.playground.grid.dataView.getItem)
            .map('tdpId')
            .sortBy('tdpId')
            .value();
    }

    /**
     * @ngdoc method
     * @name replaceRecords
     * @methodOf data-prep.services.playground.service:PreviewService
     * @description
     * <ul>
     *     <li>Copy the row list</li>
     *     <li>Insert the viable preview rows into the new list</li>
     *     <li>Display the new records</li>
     * </ul>
     */
    function replaceRecords(response) {
        DatagridService.execute(reverter);
        var executor = DatagridService.previewDataExecutor(response.data);
        reverter = DatagridService.execute(executor);
    }

    /**
     * @ngdoc method
     * @name initPreviewIdNeeded
     * @methodOf data-prep.services.playground.service:PreviewService
     * @description Init the TDP ids, the start/end ids if no preview is currently displayed and create a new preview canceler promise
     */
    function initPreviewIdNeeded() {
        if (!originalData) {
            originalData = {
                columns: state.playground.data.metadata.columns.slice(0),
                records: state.playground.data.records.slice(0)
            };
            displayedTdpIds = getDisplayedTdpIds();
        }

        previewCanceler = $q.defer();
    }

    /**
     * @ngdoc method
     * @name getPreviewDiffRecords
     * @methodOf data-prep.services.playground.service:PreviewService
     * @param {string} preparationId The preparation id
     * @param {object} currentStep The current active step
     * @param {object} previewStep The step to preview
     * @param {string} targetColumnId The column id to focus on
     * @description Call the diff preview service and replace records in the grid.
     * It cancel the previous preview first
     */
    function getPreviewDiffRecords(preparationId, currentStep, previewStep, targetColumnId) {
        stopPendingPreview();
        initPreviewIdNeeded();

        var params = {
            preparationId: preparationId,
            currentStepId: currentStep.transformation.stepId,
            previewStepId: previewStep.transformation.stepId,
            tdpIds: displayedTdpIds
        };
        PreparationService.getPreviewDiff(params, previewCanceler)
            .then(function (response) {
                DatagridService.focusedColumn = targetColumnId;
                return response;
            })
            .then(replaceRecords)
            .finally(function () {
                previewCanceler = null;
            });
    }

    /**
     * @ngdoc method
     * @name getPreviewUpdateRecords
     * @methodOf data-prep.services.playground.service:PreviewService
     * @param {string} preparationId The preparation id
     * @param {object} currentStep The current active step
     * @param {object} updateStep The step to update for the preview preview
     * @param {object} newParams The new parameters to apply on the step to update
     * @description Call the update step preview service and replace records in the grid.
     * It cancel the previous preview first
     */
    function getPreviewUpdateRecords(preparationId, currentStep, updateStep, newParams) {
        stopPendingPreview();
        initPreviewIdNeeded();

        var params = {
            preparationId: preparationId,
            tdpIds: displayedTdpIds,
            currentStepId: currentStep.transformation.stepId,
            updateStepId: updateStep.transformation.stepId,
            action: {
                action: updateStep.actionParameters.action,
                parameters: newParams
            }
        };
        PreparationService.getPreviewUpdate(params, previewCanceler)
            .then(function (response) {
                DatagridService.focusedColumn = updateStep.column.id;
                return response;
            })
            .then(replaceRecords)
            .finally(function () {
                previewCanceler = null;
            });
    }

    /**
     * @ngdoc method
     * @name getPreviewUpdateRecords
     * @methodOf data-prep.services.playground.service:PreviewService
     * @param {string} preparationId The preparation id
     * @param {object} datasetId The dataset id
     * @param {object} action The action to append
     * @param {object} actionParams The action parameters
     * @description Call the update step preview service and replace records in the grid.
     * It cancel the previous preview first
     */
    function getPreviewAddRecords(preparationId, datasetId, action, actionParams) {
        stopPendingPreview();
        initPreviewIdNeeded();

        var params = {
            action: {
                action: action,
                parameters: actionParams
            },
            tdpIds: displayedTdpIds,
            datasetId: datasetId,
            preparationId: preparationId
        };
        PreparationService.getPreviewAdd(params, previewCanceler)
            .then(function (response) {
                DatagridService.focusedColumn = actionParams.column_id;
                return response;
            })
            .then(replaceRecords)
            .finally(function () {
                previewCanceler = null;
            });
    }

    /**
     * @ngdoc method
     * @name stopPendingPreview
     * @methodOf data-prep.services.playground.service:PreviewService
     * @description Cancel the pending preview (resolving the cancel promise).
     */
    function stopPendingPreview() {
        if (previewCanceler) {
            previewCanceler.resolve('user cancel');
            previewCanceler = null;
        }
    }

    /**
     * @ngdoc method
     * @name reset
     * @methodOf data-prep.services.playground.service:PreviewService
     * @param {boolean} restoreOriginalData If true, restore the original data before the reset
     * @description Reset the variables (original data, ids, ...), and optionally restore the original records
     */
    function reset(restoreOriginalData) {
        if (restoreOriginalData && previewInProgress()) {
            DatagridService.execute(reverter);
        }
        originalData = null;
        displayedTdpIds = null;
        reverter = null;
    }

    /**
     * @ngdoc method
     * @name cancelPreview
     * @param {string} focusedColId The column id where to set the grid focus
     * @methodOf data-prep.services.playground.service:PreviewService
     * @description Cancel the current preview or the pending preview (resolving the cancel promise).
     * The original records is set back into the datagrid
     */
    function cancelPreview(focusedColId) {
        stopPendingPreview();

        DatagridService.focusedColumn = focusedColId;
        reset(true);
    }

    /**
     * @ngdoc method
     * @name previewInProgress
     * @methodOf data-prep.services.playground.service:PreviewService
     * @description Test if a preview is currently displayed
     */
    function previewInProgress() {
        return !!originalData;
    }
}