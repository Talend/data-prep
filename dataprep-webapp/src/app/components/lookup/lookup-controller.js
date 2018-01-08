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
 * @name data-prep.lookup
 * @description Lookup controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.lookup.service:LookupService
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.early-preview.service:EarlyPreviewService
 * @requires data-prep.services.preview.service:PreviewService
 * @requires data-prep.services.utils.service:StorageService
 */
export default function LookupCtrl($timeout, state, StateService,
                                   HelpService, LookupService, PlaygroundService,
                                   EarlyPreviewService, PreviewService, StorageService) {
	'ngInject';

	const vm = this;
	vm.state = state;
	vm.cancelEarlyPreview = EarlyPreviewService.cancelEarlyPreview;
	vm.fetchLookupDatasetContent = LookupService.fetchLookupDatasetContent;
	vm.loadFromAction = LookupService.loadFromAction;
	vm.addLookupDatasetModal = false;
	vm.HelpService = HelpService;

	/**
	 * @ngdoc method
	 * @methodOf data-prep.lookup.controller:LookupCtrl
	 * @name load
	 * @description it loads the lookup params from the step or from the action
	 * */
	vm.load = function whereToLoadFrom(item) {
		if (state.playground.stepInEditionMode) {
			const lookupDatasetId = item.parameters
				.find(param => param.name === 'lookup_ds_id')
				.default;
			vm.fetchLookupDatasetContent(lookupDatasetId, item);
		}
		else {
			vm.loadFromAction(item);
		}
	};

	/**
	 * @ngdoc method
	 * @methodOf data-prep.lookup.controller:LookupCtrl
	 * @name refreshLookupDatasetsSort
	 * @description refresh the actual sort parameter
	 * */
	function refreshLookupDatasetsSort() {
		const savedSort = StorageService.getLookupDatasetsSort();
		if (savedSort) {
			StateService.setLookupDatasetsSort(_.find(state.playground.lookup.sortList, { id: savedSort }));
		}
	}

	/**
	 * @ngdoc method
	 * @methodOf data-prep.lookup.controller:LookupCtrl
	 * @name refreshLookupDatasetsOrder
	 * @description refresh the actual order parameter
	 */
	function refreshLookupDatasetsOrder() {
		const savedSortOrder = StorageService.getLookupDatasetsOrder();
		if (savedSortOrder) {
			StateService.setLookupDatasetsOrder(_.find(state.playground.lookup.orderList, { id: savedSortOrder }));
		}
	}

	/**
	 * @ngdoc method
	 * @name hoverSubmitBtn
	 * @methodOf data-prep.lookup.controller:LookupCtrl
	 * @description trigger a preview mode on the main dataset to show the lookup action effet
	 */
	vm.hoverSubmitBtn = function hoverSubmitBtn() {
		if (state.playground.stepInEditionMode) {
			PreviewService.updatePreview(state.playground.stepInEditionMode, getParams());
		}
		else {
			const previewClosure = EarlyPreviewService.earlyPreview(state.playground.lookup.dataset, 'dataset');
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
	vm.getDsName = function getDsName(item) {
		return _.find(item.parameters, { name: 'lookup_ds_name' }).default;
	};

	/**
	 * @ngdoc method
	 * @name extractLookupParams
	 * @methodOf data-prep.lookup.controller:LookupCtrl
	 * @param {object} dsLookup dataset lookup action
	 * @returns {object} the params object
	 * @description loops over the dataset lookup action parameters to collect the params
	 */
	function extractLookupParams(dsLookup) {
		return _.reduce(dsLookup.parameters, function (res, param) {
			res[param.name] = param.default;
			return res;
		}, {});
	}

	/**
	 * @ngdoc method
	 * @name getParams
	 * @methodOf data-prep.lookup.controller:LookupCtrl
	 * @description returns the params of the lookup action
	 */
	function getParams() {
		const params = extractLookupParams(state.playground.lookup.dataset);
		params.column_id = state.playground.grid.selectedColumns[0].id;
		params.column_name = state.playground.grid.selectedColumns[0].name;
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
		StateService.setPreviewDisabled(true);
		EarlyPreviewService.cancelPendingPreview();
		let promise;
		const lookupStep = state.playground.stepInEditionMode;

		if (lookupStep) {
			promise = PlaygroundService.updateStep(lookupStep, getParams());
		}
		else {
			promise = PlaygroundService.completeParamsAndAppend(state.playground.lookup.dataset, 'dataset', getParams());
		}

		promise.then(StateService.setLookupVisibility.bind(null, false))
			.finally(function () {
				$timeout(() => StateService.setPreviewDisabled(false), 500, false);
			});
	};

	/**
	 * @ngdoc method
	 * @name openAddLookupDatasetModal
	 * @methodOf data-prep.lookup.controller:LookupCtrl
	 * @description Open the add lookup dataset modal
	 */
	vm.openAddLookupDatasetModal = function openAddLookupDatasetModal() {
		LookupService.disableDatasetsUsedInRecipe();
		vm.addLookupDatasetModal = true;
	};

	/**
	 * @ngdoc method
	 * @name addLookupDatasets
	 * @methodOf data-prep.lookup.controller:LookupCtrl
	 * @description Add datasets to the lookup
	 */
	vm.addLookupDatasets = function addLookupDatasets() {
		LookupService.updateLookupDatasets();
		vm.addLookupDatasetModal = false;

		// refresh lookup panel by selecting the first action
		if (state.playground.lookup.addedActions.length > 0) {
			LookupService.loadFromAction(state.playground.lookup.addedActions[0]);
		}
	};

	/**
	 * @ngdoc method
	 * @name sort
	 * @methodOf data-prep.lookup.controller:LookupCtrl
	 * @description sort dataset by sortType by calling refreshDatasets from DatasetService
	 * @param {object} sortType Criteria to sort
	 */
	vm.updateSortBy = function updateSortBy(sortType) {
		$timeout(function () {
			StateService.setLookupDatasetsSort(sortType);
			StorageService.setLookupDatasetsSort(sortType.id);
		});
	};

	/**
	 * @ngdoc method
	 * @name sort
	 * @methodOf data-prep.lookup.controller:LookupCtrl
	 * @description sort dataset in order (ASC or DESC) by calling refreshDatasets from DatasetService
	 * @param {object} order Sort order ASC(ascending) or DESC(descending)
	 */
	vm.updateSortOrder = function updateSortOrder(order) {
		$timeout(function () {
			StateService.setLookupDatasetsOrder(order);
			StorageService.setLookupDatasetsOrder(order.id);
		});
	};

	refreshLookupDatasetsSort();
	refreshLookupDatasetsOrder();
}
