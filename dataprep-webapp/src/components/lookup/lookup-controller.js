(function() {
	'use strict';

	/**
	 * @ngdoc controller
	 * @name data-prep.lookup
	 * @description Lookup controller.
	 * @requires data-prep.services.state.constant:state
	 * @requires data-prep.services.state.service:StateService
	 * @requires data-prep.services.lookup.service:LookupService
	 * @requires data-prep.services.playground.service:EarlyPreviewService
	 * @requires data-prep.services.transformation.service:TransformationApplicationService
	 */
	function LookupCtrl($scope, state, StateService, LookupService, EarlyPreviewService, TransformationApplicationService) {
		var vm = this;
		vm.state = state;
		vm.cancelEarlyPreview = EarlyPreviewService.cancelEarlyPreview;

		/**
		 * @ngdoc method
		 * @name hoverSubmitBtn
		 * @methodOf data-prep.lookup.controller:LookupCtrl
		 * @description trigger a preview mode on the main dataset to show the lookup action effet
		 */
		vm.hoverSubmitBtn = function hoverSubmitBtn(){
			var previewClosure = EarlyPreviewService.earlyPreview(vm.lookupAction, 'dataset');
			populateParams();
			previewClosure(vm.lookupParams);
		};

		/**
		 * @ngdoc method
		 * @name extractLookupParams
		 * @methodOf data-prep.lookup.controller:LookupCtrl
		 * @param {object} dsLookup dataset lookup action
		 * @returns {object} the params object
		 * @description loops over the dataset lookup action parameters to collect the params
		 */
		function extractLookupParams (dsLookup){
			return _.reduce(dsLookup.parameters, function(res, param){
				res[param.name] = param.default;
				return res;
			},{});
		}

		/**
		 * @ngdoc method
		 * @name loadSelectedLookupContent
		 * @methodOf data-prep.lookup.controller:LookupCtrl
		 * @param {object} dsLookup dataset lookup action
		 * @description loads the content of the selected lookup dataset
		 */
		vm.loadSelectedLookupContent = function(lookupDs){
			StateService.resetLookup();
			vm.lookupParams = extractLookupParams(lookupDs);
			vm.lookupAction = lookupDs;
			/*jshint camelcase: false */
			LookupService.loadLookupContent(vm.lookupParams.lookup_ds_url);
		};

		/**
		 * @ngdoc method
		 * @name populateParams
		 * @methodOf data-prep.lookup.controller:LookupCtrl
		 * @description populates the params object by collection the needed parameters
		 */
		function populateParams () {
			/*jshint camelcase: false */
			vm.lookupParams.column_id = vm.state.playground.grid.selectedColumn.id;
			vm.lookupParams.column_name = vm.state.playground.grid.selectedColumn.name;
			vm.lookupParams.lookup_join_on = vm.state.playground.lookupGrid.selectedColumn.id;
			vm.lookupParams.lookup_join_on_name = vm.state.playground.lookupGrid.selectedColumn.name;
			vm.lookupParams.lookup_selected_cols = vm.state.playground.lookupGrid.lookupColumnsToAdd;
		}

		/**
		 * @ngdoc method
		 * @name submitLookup
		 * @methodOf data-prep.lookup.controller:LookupCtrl
		 * @description submits the lookup action
		 */
		vm.submitLookup = function submitLookup() {
			EarlyPreviewService.deactivatePreview();
			EarlyPreviewService.cancelPendingPreview();
			populateParams();

			TransformationApplicationService.append(vm.lookupAction, 'dataset', vm.lookupParams)
				.finally(function() {
					setTimeout(EarlyPreviewService.activatePreview, 500);
				});
		};

		//*****************************************************************************************//
		//**************************** Watcher on the current dataset *****************************//
		//*****************************************************************************************//
		$scope.$watch(function(){
			return vm.state.playground.dataset;
		},
		function(newDataset){
			if(newDataset){
				LookupService.getLookupPossibleActions(vm.state.playground.dataset.id)
					.then(function(dsLookup){
						vm.potentialTransformations = dsLookup.data;
						if(vm.potentialTransformations.length){
							vm.loadSelectedLookupContent(vm.potentialTransformations[0]);
						}
					});
			}
		});
	}

	angular.module('data-prep.lookup')
		.controller('LookupCtrl', LookupCtrl);
})();
