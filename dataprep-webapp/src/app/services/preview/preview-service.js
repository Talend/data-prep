/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { range, chain, map } from 'lodash';

/**
 * @ngdoc service
 * @name data-prep.services.preview.service:PreviewService
 * @description Preview service. This service holds the preview datagrid (SlickGrid) view
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.playground.service:DatagridService
 * @requires data-prep.services.preparation.service:PreparationService
 * @requires data-prep.services.utils.service:StepUtilsService
 */
export default function PreviewService($q, state, DatagridService, PreparationService, StepUtilsService) {
	'ngInject';

    /**
     * @ngdoc property
     * @name reverter
     * @propertyOf data-prep.services.preview.service:PreviewService
     * @description [PRIVATE] The revert executor
     * to apply on datagrid to go from current preview to original data
     */
	let reverter;

    /**
     * @ngdoc property
     * @name originalData
     * @propertyOf data-prep.services.preview.service:PreviewService
     * @description [PRIVATE] The original data (columns and records) before switching to preview
     */
	let originalData;

    /**
     * @ngdoc property
     * @name displayedTdpIds
     * @propertyOf data-prep.services.preview.service:PreviewService
     * @description [PRIVATE] The list of records TDP indexes that is displayed in the viewport
     */
	let displayedTdpIds;

    /**
     * @ngdoc property
     * @name previewCanceler
     * @propertyOf data-prep.services.preview.service:PreviewService
     * @description [PRIVATE] The preview cancel promise. When it is resolved, the pending request is rejected
     */
	let previewCanceler;

	const service = {
        /**
         * @ngdoc property
         * @name gridRangeIndex
         * @propertyOf data-prep.services.preview.service:PreviewService
         * @description The grid displayed rows id. It take filters into account.
         * This is updated by the {@link data-prep.datagrid.directive:Datagrid Datagrid} directive on scroll
         */
		gridRangeIndex: null,

		updatePreview,
		getPreviewDiffRecords,
		getPreviewUpdateRecords,
		getPreviewAddRecords,

		previewInProgress,
		stopPendingPreview,
		reset,
		cancelPreview,
	};
	return service;

    /**
     * @ngdoc method
     * @name getDisplayedTdpIds
     * @methodOf data-prep.services.preview.service:PreviewService
     * @description [PRIVATE] Get the rows TDP ids in the range
     * @returns {object[]} The rows TDP ids in the range
     */
	function getDisplayedTdpIds() {
		const indexes = range(service.gridRangeIndex.top, service.gridRangeIndex.bottom + 1);
		return chain(indexes)
            .map(state.playground.grid.dataView.getItem)
            .map('tdpId')
            .sortBy('tdpId')
            .value();
	}

    /**
     * @ngdoc method
     * @name replaceRecords
     * @methodOf data-prep.services.preview.service:PreviewService
     * @description
     * <ul>
     *     <li>Copy the row list</li>
     *     <li>Insert the viable preview rows into the new list</li>
     *     <li>Display the new records</li>
     * </ul>
     */
	function replaceRecords(response) {
		DatagridService.execute(reverter);
		const executor = DatagridService.previewDataExecutor(response.data);
		reverter = DatagridService.execute(executor);
	}

    /**
     * @ngdoc method
     * @name initPreviewIdNeeded
     * @methodOf data-prep.services.preview.service:PreviewService
     * @description Init the TDP ids, the start/end ids
     * if no preview is currently displayed
     * and create a new preview canceler promise
     */
	function initPreviewIdNeeded() {
		if (!originalData) {
			originalData = {
				columns: state.playground.data.metadata.columns.slice(0),
				records: state.playground.data.records.slice(0),
			};
			displayedTdpIds = getDisplayedTdpIds();
		}

		previewCanceler = $q.defer();
	}

    /**
     * @ngdoc method
     * @name updatePreview
     * @methodOf data-prep.services.preview.service:PreviewService
     * @param {string} updateStep The step position index to update for the preview
     * @param {object} params The new step params
     * @description [PRIVATE] Call the preview service to display the diff between the original steps and the updated
     *     steps
     */
	function updatePreview(updateStep, params) {
		if (!state.playground.grid.nbLines) {
			return $q.when();
		}

		const originalParameters = updateStep.actionParameters.parameters;
		PreparationService.copyImplicitParameters(params, originalParameters);

        // Parameters has not changed
		if (updateStep.inactive || !PreparationService.paramsHasChanged(updateStep, params)) {
			return $q.when();
		}

		const currentStep = StepUtilsService.getLastActiveStep(state.playground.recipe);
		const preparationId = state.playground.preparation.id;
		return service.getPreviewUpdateRecords(preparationId, currentStep, updateStep, params);
	}

    /**
     * @ngdoc method
     * @name getPreviewDiffRecords
     * @methodOf data-prep.services.preview.service:PreviewService
     * @param {string} preparationId The preparation id
     * @param {object} currentStep The current active step
     * @param {object} previewStep The step to preview
     * @param {string} targetColumnId The column id to focus on
     * @description Call the diff preview service and replace records in the grid.
     * It cancel the previous preview first
     */
	function getPreviewDiffRecords(preparationId, currentStep, previewStep, targetColumnId) {
		if (!state.playground.grid.nbLines) {
			return $q.when();
		}

		stopPendingPreview();
		initPreviewIdNeeded();

		const params = {
			preparationId,
			currentStepId: currentStep.transformation.stepId,
			previewStepId: previewStep.transformation.stepId,
			tdpIds: displayedTdpIds,
			sourceType: state.playground.sampleType,
		};
		return PreparationService.getPreviewDiff(params, previewCanceler)
            .then(function (response) {
	DatagridService.focusedColumn = targetColumnId;
	return response;
})
            .then(replaceRecords)
            .catch((e) => {
	cancelPreview();
	return $q.reject(e);
})
            .finally(() => previewCanceler = null);
	}

    /**
     * @ngdoc method
     * @name getPreviewUpdateRecords
     * @methodOf data-prep.services.preview.service:PreviewService
     * @param {string} preparationId The preparation id
     * @param {object} currentStep The current active step
     * @param {object} updateStep The step to update for the preview preview
     * @param {object} newParams The new parameters to apply on the step to update
     * @description Call the update step preview service and replace records in the grid.
     * It cancel the previous preview first
     */
	function getPreviewUpdateRecords(preparationId, currentStep, updateStep, newParams) {
		if (!state.playground.grid.nbLines) {
			return $q.when();
		}

		stopPendingPreview();
		initPreviewIdNeeded();

		const params = {
			preparationId,
			tdpIds: displayedTdpIds,
			currentStepId: currentStep.transformation.stepId,
			updateStepId: updateStep.transformation.stepId,
			action: {
				action: updateStep.actionParameters.action,
				parameters: newParams,
			},
			sourceType: state.playground.sampleType,
		};
		return PreparationService.getPreviewUpdate(params, previewCanceler)
            .then(function (response) {
	DatagridService.focusedColumn = updateStep.column.id;
	return response;
})
            .then(replaceRecords)
            .catch((e) => {
	cancelPreview();
	return $q.reject(e);
})
            .finally(() => previewCanceler = null);
	}

    /**
     * @ngdoc method
     * @name getPreviewUpdateRecords
     * @methodOf data-prep.services.preview.service:PreviewService
     * @param {string} preparationId The preparation id
     * @param {object} datasetId The dataset id
     * @param {string} action The action name
     * @param {Array} actionParams The action parameters list
     * @description Call the update step preview service and replace records in the grid.
     * It cancel the previous preview first
     */
	function getPreviewAddRecords(preparationId, datasetId, action, actionParams) {
		if (!state.playground.grid.nbLines) {
			return $q.when();
		}

		stopPendingPreview();
		initPreviewIdNeeded();

		const params = {
			actions: map(actionParams, parameters => ({ action, parameters })),
			tdpIds: displayedTdpIds,
			datasetId,
			preparationId,
			sourceType: state.playground.sampleType,
		};
		return PreparationService.getPreviewAdd(params, previewCanceler)
            .then(function (response) {
	DatagridService.focusedColumn = actionParams[0].column_id;
	return response;
})
            .then(replaceRecords)
            .catch((e) => {
	cancelPreview();
	return $q.reject(e);
})
            .finally(() => previewCanceler = null);
	}

    /**
     * @ngdoc method
     * @name stopPendingPreview
     * @methodOf data-prep.services.preview.service:PreviewService
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
     * @methodOf data-prep.services.preview.service:PreviewService
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
     * @methodOf data-prep.services.preview.service:PreviewService
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
     * @methodOf data-prep.services.preview.service:PreviewService
     * @description Test if a preview is currently displayed
     */
	function previewInProgress() {
		return !!originalData;
	}
}
