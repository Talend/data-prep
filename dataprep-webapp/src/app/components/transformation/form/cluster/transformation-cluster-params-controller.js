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
 * @name data-prep.transformation-form.controller:TransformClusterParamsCtrl
 * @description Transformation cluster parameters controller.
 */
export default function TransformClusterParamsCtrl() {
	const vm = this;

    /**
     * @ngdoc property
     * @name allCheckboxState
     * @propertyOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
     * @description The global checkbox state
     */
	vm.allCheckboxState = true;

    /**
     * @ngdoc method
     * @name refreshClusterState
     * @methodOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
     * @description Refresh all cluster "active" flag to the state of the global activation checkbox
     */
	vm.refreshClusterState = function refreshClusterState() {
		_.forEach(vm.details.clusters, (cluster) => {
			cluster.active = vm.allCheckboxState;
		});
	};

    /**
     * @ngdoc method
     * @name initClusterState
     * @methodOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
     * @description Initialize all cluster "active" flag
     */
	vm.initClusterState = function initClusterState() {
		_.forEach(vm.details.clusters, (cluster) => {
			cluster.active = cluster.initialActive;
		});
	};

    /**
     * @ngdoc method
     * @name getParams
     * @methodOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
     * @description Refresh the global activation checkbox
     */
	vm.refreshToggleCheckbox = function refreshToggleCheckbox() {
		const inactiveCluster = _.find(vm.details.clusters, cluster => !cluster.active);
		vm.allCheckboxState = !inactiveCluster;
	};

    /**
     * @ngdoc method
     * @name initParamsValues
     * @methodOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
     * @description Initialize parameters values and checkbox state if needed
     */
	function initParamsValues() {
		_.forEach(vm.details.clusters, (cluster) => {
			_.forEach(cluster.parameters, (param) => {
				param.default = true;
			});
		});
	}

    /**
     * @ngdoc method
     * @name initActivationFlags
     * @methodOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
     * @description Initialize clusters activation flag
     */
	function initActivationFlags() {
		const hasInitialActive = _.find(vm.details.clusters, (cluster) => {
			return typeof cluster.initialActive !== 'undefined';
		});

		if (!hasInitialActive) {
			vm.refreshClusterState();
		}
		else {
			vm.initClusterState();
		}
	}

    /**
     * @ngdoc method
     * @name initInputTypes
     * @methodOf data-prep.transformation-form.controller:TransformSimpleParamsCtrl
     * @description [PRIVATE] Init params input type, depending on param type
     */
	function initReplaceList() {
		_.forEach(vm.details.clusters, (cluster) => {
			cluster.replace.list = _.map(cluster.parameters, 'name');
		});
	}

	initParamsValues();
	initReplaceList();
	initActivationFlags();
}
