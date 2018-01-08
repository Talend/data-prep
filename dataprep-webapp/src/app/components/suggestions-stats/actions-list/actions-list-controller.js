/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
 * @description Actions suggestion controller
 * @requires data-prep.services.transformation.service:TransformationService
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.early-preview.service:EarlyPreviewService
 */
export default function ActionsListCtrl($timeout, state, TransformationService,
                                        PlaygroundService, EarlyPreviewService, StateService) {
	'ngInject';

	const vm = this;
	vm.state = state;

	vm.cancelEarlyPreview = EarlyPreviewService.cancelEarlyPreview;
	vm.earlyPreview = function earlyPreview(action) {
		return EarlyPreviewService.earlyPreview(action, vm.scope);
	};

	/**
	 * @ngdoc property
	 * @name transformationInProgress
	 * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
	 * @description Flag that indicates if a transformation is in progress
	 */
	vm.transformationInProgress = false;

	/**
	 * @ngdoc property
	 * @name dynamicTransformation
	 * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
	 * @description The dynamic param transformation to display
	 */
	vm.dynamicTransformation = null;

	/**
	 * @ngdoc property
	 * @name dynamicFetchInProgress
	 * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
	 * @description Flag that indicates if a fetch of dynamic parameters is in progress
	 */
	vm.dynamicFetchInProgress = false;

	/**
	 * @ngdoc property
	 * @name showDynamicModal
	 * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
	 * @description Flag that change the dynamic parameters modal display
	 */
	vm.showDynamicModal = false;

	/**
	 * @ngdoc property
	 * @name showModalContent
	 * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
	 * @description show/hides the dynamic transformation or the alert message
	 */
	vm.showModalContent = null;

	/**
	 * @ngdoc method
	 * @name initDynamicParams
	 * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
	 * @description [PRIVATE] Fetch the transformation dynamic parameters and inject them into transformation menu params
	 * @returns {promise} The GET request promise
	 */
	const initDynamicParams = function (action) {
		const infos = {
			columnId: state.playground.grid.selectedColumns[0].id,
			datasetId: state.playground.dataset.id,
			preparationId: state.playground.preparation ? state.playground.preparation.id : null,
		};
		return TransformationService.initDynamicParameters(action, infos);
	};

	/**
	 * @ngdoc method
	 * @name checkDynamicResponse
	 * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
	 * @description [PRIVATE] sets the showHideModalContent and the emptyParamsMsg properties
	 */
	function checkDynamicResponse() {
		// transformation type :cluster
		if (vm.dynamicTransformation.cluster) {
			if (vm.dynamicTransformation.cluster.clusters.length) {
				vm.showModalContent = true;
			}
			else {
				vm.showModalContent = false;
				vm.emptyParamsMsg = 'NO_CLUSTERS_ACTION_MSG';
			}
		}

		// transformation type :simpleParams
		else if (vm.dynamicTransformation.parameters) {
			if (vm.dynamicTransformation.parameters.length) {
				vm.showModalContent = true;
			}
			else {
				vm.showModalContent = false;
				vm.emptyParamsMsg = 'NO_PARAMETERS_ACTION_MSG';
			}
		}
	}

	/**
	 * @ngdoc method
	 * @name select
	 * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
	 * @description Transformation selection.
	 <ul>
	 <li>Dynamic transformation : fetch the dynamic parameters and show the modal</li>
	 <li>Static transformation : append the new step in the current preparation</li>
	 </ul>
	 * @param {object} transformation The selected transformation
	 */
	vm.select = function select(transformation) {
		if (transformation.dynamic) {
			vm.dynamicTransformation = transformation;
			vm.dynamicFetchInProgress = true;
			vm.showDynamicModal = true;

			// get new parameters
			initDynamicParams(transformation).finally(function () {
				checkDynamicResponse();
				vm.dynamicFetchInProgress = false;
			});
		}
		else {
			vm.transform(transformation, vm.scope)();
		}
	};

	/**
	 * @ngdoc method
	 * @name transform
	 * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
	 * @description Apply a transformation
	 */
	vm.transform = function transform(action) {
		return function (params) {
			StateService.setPreviewDisabled(true);
			EarlyPreviewService.cancelPendingPreview();

			if (!vm.transformationInProgress) {
				vm.transformationInProgress = true;
				PlaygroundService.completeParamsAndAppend(action, vm.scope, params)
					.then(function () {
						vm.showDynamicModal = false;
					})
					.finally(function () {
						$timeout(() => {
							StateService.setPreviewDisabled(false);
							vm.transformationInProgress = false;
						}, 500, false);
					});
			}
		};
	};
}
