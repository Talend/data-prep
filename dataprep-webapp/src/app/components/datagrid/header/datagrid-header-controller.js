/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import {
	INVALID_RECORDS,
	EMPTY_RECORDS,
	INVALID_EMPTY_RECORDS,
	VALID_RECORDS,
} from '../../../services/filter/adapter/filter-adapter-service';

/**
 * @ngdoc controller
 * @name data-prep.datagrid-header.controller:DatagridHeaderCtrl
 * @description Dataset Column Header controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.column-types.service:ColumnTypesService
 * @requires data-prep.services.transformation.service:TransformationService
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.filter.service:FilterService
 */
export default function DatagridHeaderCtrl($scope, state,
                                           TransformationService, ConverterService,
                                           PlaygroundService, FilterService,
                                           ColumnTypesService, FilterManagerService) {
	'ngInject';

	const ACTION_SCOPE = 'column_metadata';
	const RENAME_ACTION = 'rename_column';
	let originalName;

	const vm = this;
	vm.converterService = ConverterService;
	vm.filterManagerService = FilterManagerService;
	vm.PlaygroundService = PlaygroundService;
	vm.state = state;

	vm.VALID_RECORDS = VALID_RECORDS;
	vm.INVALID_RECORDS = INVALID_RECORDS;
	vm.EMPTY_RECORDS = EMPTY_RECORDS;
	vm.INVALID_EMPTY_RECORDS = INVALID_EMPTY_RECORDS;

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
	vm.initTransformations = () => {
		if (!state.playground.isReadOnly && !vm.transformations && !vm.initTransformationsInProgress) {
			vm.transformationsRetrieveError = false;
			vm.initTransformationsInProgress = true;

			TransformationService.getTransformations('column', vm.column)
				.then((columnTransformations) => {
					vm.transformations = columnTransformations
						.allTransformations
						.filter(menu => menu.actionScope.indexOf(ACTION_SCOPE) !== -1);
				})
				.catch(() => {
					vm.transformationsRetrieveError = true;
				})
				.finally(() => {
					vm.initTransformationsInProgress = false;
				});
		}
		ColumnTypesService.refreshSemanticDomains(vm.column.id);
		ColumnTypesService.refreshTypes();
	};

	/**
	 * @ngdoc method
	 * @name updateColumnName
	 * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description update the new column name
	 */
	vm.updateColumnName = () => {
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
	vm.nameHasChanged = () => {
		return vm.newName && originalName !== vm.newName;
	};


	/**
	 * @ngdoc method
	 * @name setEditMode
	 * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description Set isEditMode to provided value
	 * @param {boolean} bool The new edit mode value
	 */
	vm.setEditMode = (bool) => {
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
	vm.resetColumnName = () => {
		vm.newName = originalName;
	};

	/**
	 * @ngdoc method
	 * @name addFilter
	 * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description add filter
	 */
	vm.addFilter = (type) => {
		vm.filterManagerService.addFilter(type, vm.column.id, vm.column.name);
	};

	/**
	 * @ngdoc method
	 * @name getTypeLabel
	 * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
	 * @description returns the type label
	 */
	vm.getTypeLabel = () => {
		return vm.column.domainLabel || vm.converterService.simplifyTypeLabel(vm.column.type);
	};

	/**
	 * Invalidate transformations if a column has been modified
	 * e.g. its name
	 */
	$scope.$watch(
		() => vm.column,
		() => {
			vm.transformations = null;
		}
	);
}
