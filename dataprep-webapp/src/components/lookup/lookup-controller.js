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
	function LookupCtrl(state, StateService, LookupService, EarlyPreviewService,
						TransformationApplicationService, PlaygroundService) {
		var vm = this;
		vm.state = state;
		vm.cancelEarlyPreview = EarlyPreviewService.cancelEarlyPreview;
		vm.loadFromAction= LookupService.loadFromAction;

		/**
		 * @ngdoc method
		 * @name hoverSubmitBtn
		 * @methodOf data-prep.lookup.controller:LookupCtrl
		 * @description trigger a preview mode on the main dataset to show the lookup action effet
		 */
		vm.hoverSubmitBtn = function hoverSubmitBtn(){
			if (state.playground.lookup.step) {
				PlaygroundService.updatePreview(state.playground.lookup.step, getParams());
			} else {
				var previewClosure = EarlyPreviewService.earlyPreview(state.playground.lookup.dataset, 'dataset');
				previewClosure(getParams());
			}
		};

		/**
		 * @ngdoc method
		 * @name getDsName
		 * @methodOf data-prep.lookup.controller:LookupCtrl
		 * @param {object} item dataset lookup action
		 * @returns {String} the name of th dataset to be shown in the list
		 * @description loops over the dataset lookup action parameters to collect the dataset name
		 */
		vm.getDsName = function getDsName (item){
			return _.find(item.parameters, {name:'lookup_ds_name'}).default;
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
		 * @name getParams
		 * @methodOf data-prep.lookup.controller:LookupCtrl
		 * @description returns the params of the lookup action
		 */
		function getParams () {
			var params = extractLookupParams(state.playground.lookup.dataset);
			/*jshint camelcase: false */
			params.column_id = state.playground.grid.selectedColumn.id;
			params.column_name = state.playground.grid.selectedColumn.name;
			params.lookup_join_on = state.playground.lookup.selectedColumn.id;
			params.lookup_join_on_name = state.playground.lookup.selectedColumn.name;
			params.lookup_selected_cols = state.playground.lookup.columnsToAdd;
			return params;
		}

		/**
		 * @ngdoc method
		 * @name submit
		 * @methodOf data-prep.lookup.controller:LookupCtrl
		 * @description submits the lookup action
		 */
		vm.submit = function submit() {
			EarlyPreviewService.deactivatePreview();
			EarlyPreviewService.cancelPendingPreview();
			var promise;
			var lookupStep = vm.state.playground.lookup.step;

			if (lookupStep) {
				promise = PlaygroundService.updateStep(lookupStep, getParams());
			}
			else {
				promise = TransformationApplicationService.append(state.playground.lookup.dataset, 'dataset', getParams());
			}

			promise.then(StateService.setLookupVisibility.bind(null, false))
				.finally(function () {
					setTimeout(EarlyPreviewService.activatePreview, 500);
				});
		};
	}

	angular.module('data-prep.lookup')
		.controller('LookupCtrl', LookupCtrl);
})();
