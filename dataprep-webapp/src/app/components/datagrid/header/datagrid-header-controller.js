/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { filter } from 'lodash';

/**
 * @ngdoc controller
 * @name data-prep.datagrid-header.controller:DatagridHeaderCtrl
 * @description Dataset Column Header controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.column-types.service:ColumnTypesService
 * @requires data-prep.services.transformation.service:TransformationService
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.filter.service:FilterService
 */
export default function DatagridHeaderCtrl($scope, $q, state, StateService, TransformationService, ConverterService, PlaygroundService,
                                           FilterService, ColumnTypesService, FilterManagerService) {
	'ngInject';

	const ACTION_SCOPE = 'column_metadata';
	const RENAME_ACTION = 'rename_column';
	let originalName;

	const vm = this;
	vm.converterService = ConverterService;
	vm.filterManagerService = FilterManagerService;
	vm.PlaygroundService = PlaygroundService;
	vm.state = state;

	/**
	 * @ngdoc property
	 * @name newName
	 * @propertyOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description the new column modified name
	 * @type {string}
	 */
	vm.newName = null;

	/**
	 * @ngdoc property
	 * @name isEditMode
	 * @propertyOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description the flag to switch column name edition mode
	 * @type {string}
	 */
	vm.isEditMode = false;

	/**
	 * @ngdoc method
	 * @name initTransformations
	 * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description Get transformations from REST call
	 */
	vm.initTransformations = function initTransformations() {
		if (!vm.initTransformationsInProgress) {
			vm.transformationsRetrieveError = false;
			vm.initTransformationsInProgress = true;

			TransformationService.getTransformations('column', vm.column)
				.then((columnTransformations) => {
					vm.transformations = filter(
						columnTransformations.allTransformations,
						menu => (menu.actionScope.indexOf(ACTION_SCOPE) !== -1)
					);
				})
				.catch(() => {
					vm.transformationsRetrieveError = true;
				})
				.finally(() => {
					vm.initTransformationsInProgress = false;
				});
		}
		StateService.setSemanticDomains([]);
		StateService.setPrimitiveTypes([]);
		$q.all([vm._fetchAndAdaptDomainsPromise(), vm._loadPrimitiveTypesPromise()]);
	};


	/**
	 * @ngdoc method
	 * @name _fetchAndAdaptDomainsPromise
	 * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description fetches and adapts the semantic domain list for ui
	 * @returns {promise} GET promise
	 */
	vm._fetchAndAdaptDomainsPromise = function _fetchAndAdaptDomainsPromise() {
		let inventoryType = '';
		let inventoryId = '';
		if (vm.state.playground.preparation) {
			inventoryType = 'preparation';
			inventoryId = vm.state.playground.preparation.id;
		}
		else {
			inventoryType = 'dataset';
			inventoryId = vm.state.playground.dataset.id;
		}
		return ColumnTypesService.getColSemanticDomains(inventoryType, inventoryId, vm.column.id)
			.then((semanticDomains) => {
				const domains = _.chain(semanticDomains)
					.filter('id')
					.sortBy('frequency')
					.reverse()
					.value();
				StateService.setSemanticDomains(domains);
			});
	};

	/**
	 * @ngdoc method
	 * @name _loadPrimitiveTypesPromise
	 * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description loads the primitive types
	 * @returns {promise} GET promise
	 */
	vm._loadPrimitiveTypesPromise = function _loadPrimitiveTypesPromise() {
		return ColumnTypesService.getTypes()
			.then((types) => {
				const ignoredTypes = ['double', 'numeric', 'any'];
				const remainingTypes = _.filter(types, type => ignoredTypes.indexOf(type.id.toLowerCase()) === -1);
				StateService.setPrimitiveTypes(remainingTypes);
			});
	};

	/**
	 * @ngdoc method
	 * @name updateColumnName
	 * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description update the new column name
	 */
	vm.updateColumnName = function updateColumnName() {
		const params = {
			new_column_name: vm.newName,
			scope: 'column',
			column_id: vm.column.id,
			column_name: vm.column.name,
		};

		PlaygroundService.appendStep([{ action: RENAME_ACTION, parameters: params }])
			.then(() => {
				vm.setEditMode(false);
				originalName = vm.newName;
			});
	};

	/**
	 * @ngdoc method
	 * @name nameHasChanged
	 * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description Check if the new name is correct for column name change
	 */
	vm.nameHasChanged = function nameHasChanged() {
		return vm.newName && originalName !== vm.newName;
	};


	/**
	 * @ngdoc method
	 * @name setEditMode
	 * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description Set isEditMode to provided value
	 * @param {boolean} bool The new edit mode value
	 */
	vm.setEditMode = function setEditMode(bool) {
		vm.isEditMode = bool;

		if (bool) {
			vm.newName = originalName = vm.column.name;
		}
	};


	/**
	 * @ngdoc method
	 * @name resetColumnName
	 * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description Reset newName with the original name
	 */
	vm.resetColumnName = function resetColumnName() {
		vm.newName = originalName;
	};
}
