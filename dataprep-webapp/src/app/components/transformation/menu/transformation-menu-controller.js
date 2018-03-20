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
 * @name data-prep.transformation-menu.controller:TransformMenuCtrl
 * @description Transformation menu item controller.
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.parameters.service:ParametersService
 * @requires data-prep.services.transformation.service:TransformationService
 */
export default function TransformMenuCtrl($timeout, state, PlaygroundService, ParametersService, TransformationService) {
	'ngInject';

	const vm = this;

	/**
	 * @ngdoc property
	 * @name state
	 * @propertyOf data-prep.transformation-menu.controller:TransformMenuCtrl
	 * @description State for static and semantic types
	 */
	vm.state = state;

    /**
     * @ngdoc property
     * @name transformationInProgress
     * @propertyOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @description Flag that indicates if a transformation is in progress
     */
	vm.transformationInProgress = false;

    /**
     * @ngdoc method
     * @name initDynamicParams
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @description [PRIVATE] Fetch the transformation dynamic parameters
     * and inject them into transformation menu params
     * @param {object} menu The dynamic transformation
     * @returns {promise} The GET request promise
     */
	function initDynamicParams(menu) {
		const infos = {
			columnId: vm.column.id,
			datasetId: state.playground.dataset.id,
			preparationId: state.playground.preparation ?
                state.playground.preparation.id :
                null,
		};
		return TransformationService.initDynamicParameters(menu, infos);
	}

    /**
     * @ngdoc method
     * @name select
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @description Menu selected. 3 choices :
     * <ul>
     *     <li>divider : no action</li>
     *     <li>no parameter and no choice is required : transformation call</li>
     *     <li>parameter or choice required : show modal</li>
     * </ul>
     * @param {object} menu The transformation to perform
     * @param {string} scope The transformation scope to perform
     */
	vm.select = function (menu, scope) {
		if (menu.dynamic) {
			vm.dynamicFetchInProgress = true;
			vm.showModal = true;
			vm.selectedMenu = menu;
			vm.selectedScope = scope;

            // get new parameters
			initDynamicParams(menu).finally(function () {
				vm.dynamicFetchInProgress = false;
			});
		}
		else if (menu.parameters || menu.items) {
			ParametersService.resetParamValue(menu.parameters);
			ParametersService.resetParamValue(menu.items, 'CHOICE');
			vm.showModal = true;
			vm.selectedMenu = menu;
			vm.selectedScope = scope;
		}
		else {
			vm.appendClosure(menu, scope)();
		}
	};

    /**
     * @ngdoc method
     * @name select
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @description Create a closure for a specific menu/scope,
     * that only take the parameters to perform the transformation
     * @param {object} menu The transformation to perform
     * @param {string} scope The transformation scope to perform
     */
	vm.appendClosure = function appendClosure(menu, scope) {
		return function (params) {
			if (!vm.transformationInProgress) {
				vm.transformationInProgress = true;

				params = params || {};
				params.scope = scope;
				params.column_id = vm.column.id;
				params.column_name = vm.column.name;

				transform(menu, params)
                    .finally(function () {
	$timeout(() => {
		vm.transformationInProgress = false;
	}, 500, false);
});
			}
		};
	};

    /**
     * @ngdoc method
     * @name transform
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @param {object} menu The transformation
     * @param {object} params The transformation params
     * @description Perform a transformation on the column
     */
	function transform(menu, params) {
		return PlaygroundService.appendStep([{ action: menu.name, parameters: params }])
            .then(function () {
	vm.showModal = false;
});
	}
}
