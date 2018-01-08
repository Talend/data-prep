/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { HOME_PREPARATIONS_ROUTE, PLAYGROUND_PREPARATION_ROUTE } from '../../index-route';

/**
 * @ngdoc service
 * @name data-prep.services.preparation.service:PreparationService
 * @description Preparation list service. this service manage the operations that touches the preparations
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.utils.service:StorageService
 * @requires data-prep.services.preparation.service:PreparationRestService
 */
export default function PreparationService($q, $state, $window, $stateParams, StateService, StorageService, PreparationRestService) {
	'ngInject';

	return {
		// details, content
		getContent: PreparationRestService.getContent,
		getMetadata: PreparationRestService.getMetadata,
		getDetails: PreparationRestService.getDetails,

		// preparation lifecycle
		create,
		copy: PreparationRestService.copy,
		move: PreparationRestService.move,
		delete: deletePreparation,
		setName,
		open,

		// preparation steps lifecycle
		copyImplicitParameters,
		paramsHasChanged,
		appendStep: PreparationRestService.appendStep,
		updateStep,
		moveStep: PreparationRestService.moveStep,
		removeStep: PreparationRestService.removeStep,
		setHead: PreparationRestService.setHead,
		copySteps: PreparationRestService.copySteps,

		// preview
		getPreviewDiff: PreparationRestService.getPreviewDiff,
		getPreviewUpdate: PreparationRestService.getPreviewUpdate,
		getPreviewAdd: PreparationRestService.getPreviewAdd,
	};
	//---------------------------------------------------------------------------------
	// -----------------------------------------LIFE------------------------------------
	//---------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name create
	 * @methodOf data-prep.services.preparation.service:PreparationService
	 * @param {string} datasetId The dataset id
	 * @param {string} name The preparation name
	 * @description Create a new preparation
	 * @returns {promise} The POST promise
	 */
	function create(datasetId, name, destinationFolder) {
		StateService.setPreviousRoute(HOME_PREPARATIONS_ROUTE, { folderId: $stateParams.folderId });
		return PreparationRestService.create(datasetId, name, destinationFolder)
			.then((preparationId) => {
				// get all dataset aggregations per columns from localStorage and save them for the new preparation
				StorageService.savePreparationAggregationsFromDataset(datasetId, preparationId);
				return preparationId;
			});
	}

	/**
	 * @ngdoc method
	 * @name delete
	 * @methodOf data-prep.services.preparation.service:PreparationService
	 * @param integer preparation The preparation id to delete
	 * @description Delete a preparation
	 * @returns {promise} The DELETE promise
	 */
	function deletePreparation(preparation) {
		return PreparationRestService.delete(preparation.id)
			.then((response) => {
				// get remove all preparation aggregations per columns in localStorage
				StorageService.removeAllAggregations(preparation.dataSetId, preparation.id);
				return response;
			});
	}

	//---------------------------------------------------------------------------------
	// ----------------------------------------UPDATE-----------------------------------
	//---------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name setName
	 * @methodOf data-prep.services.preparation.service:PreparationService
	 * @param {string} preparationId The preparation id
	 * @param {string} name The preparation name
	 * @description Create a new preparation if no preparation is loaded, update the name otherwise
	 * @returns {promise} The PUT promise
	 */
	function setName(preparationId, name) {
		return PreparationRestService.update(preparationId, { name })
			.then(preparationId => PreparationRestService.getDetails(preparationId));
	}

	/**
	 * @ngdoc method
	 * @name copyImplicitParameters
	 * @methodOf data-prep.services.preparation.service:PreparationService
	 * @param {object} parameters The parameters to copy into
	 * @param {object} originalParameters The original parameters containing the implicit parameters values
	 * @description Copy the original implicit parameters values into new parameters
	 */
	function copyImplicitParameters(parameters, originalParameters) {
		parameters.scope = originalParameters.scope;

		if ('column_id' in originalParameters && !('column_id' in parameters)) {
			parameters.column_id = originalParameters.column_id;
		}

		if ('column_name' in originalParameters && !('column_name' in parameters)) {
			parameters.column_name = originalParameters.column_name;
		}

		if ('row_id' in originalParameters && !('row_id' in parameters)) {
			parameters.row_id = originalParameters.row_id;
		}

		if ('filter' in originalParameters && !('filter' in parameters)) {
			parameters.filter = originalParameters.filter;
		}
	}

	/**
	 * @ngdoc method
	 * @name updateStep
	 * @methodOf data-prep.services.preparation.service:PreparationService
	 * @param {string} preparationId The preparation id
	 * @param {object} step The step to update
	 * @param {object} parameters The new action parameters
	 * @description Update a step with new parameters
	 * @returns {promise} The PUT promise
	 */
	function updateStep(preparationId, step, parameters) {
		return PreparationRestService.updateStep(
			preparationId,
			step.transformation.stepId,
			{ action: step.transformation.name, parameters }
		);
	}

	/**
	 * @ngdoc method
	 * @name paramsHasChanged
	 * @methodOf data-prep.services.preparation.service:PreparationService
	 * @param {object} step The step to update
	 * @param {object} newParams The new action parameters
	 * @description Check if the parameters has changed
	 * @returns {boolean} true if parameters has changed, false otherwise
	 */
	function paramsHasChanged(step, newParams) {
		return JSON.stringify(newParams) !== JSON.stringify(step.actionParameters.parameters);
	}


	/**
	 * @ngdoc method
	 * @name open
	 * @methodOf data-prep.services.preparation.service:PreparationService
	 * @param {object} preparation
	 * @description open a preparation
	 */
	function open(preparation) {
		$state.go(PLAYGROUND_PREPARATION_ROUTE, { prepid: preparation.id });
	}
}
