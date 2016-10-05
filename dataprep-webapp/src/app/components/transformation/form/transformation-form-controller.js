/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.transformation-form.controller:TransformFormCtrl
 * @description Transformation parameters controller.
 */
export default function TransformFormCtrl() {
	const vm = this;

    /**
     * @ngdoc method
     * @name getParamIteration
     * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
     * @description [PRIVATE] Inner function for recursively gather params
     * @param {object} paramsAccu The parameters values accumulator
     * @param {array} parameters The parameters array
     * @returns {object} The parameters
     */
	function getParamIteration(paramsAccu, parameters) {
		if (parameters) {
			_.forEach(parameters, (paramItem) => {
				paramsAccu[paramItem.name] =
                    typeof (paramItem.value) !== 'undefined' ?
                        paramItem.value :
                        paramItem.default;

                // deal with select inline parameters
				if (paramItem.type === 'select') {
					const selectedValue = _.find(
                        paramItem.configuration.values,
                        { value: paramItem.value }
                    );
					getParamIteration(paramsAccu, selectedValue.parameters);
				}
			});
		}

		return paramsAccu;
	}

    /**
     * @ngdoc method
     * @name getParams
     * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
     * @description [PRIVATE] Get item parameters into one object for REST call
     * @returns {object} - the parameters
     */
	function getParams() {
		return getParamIteration({}, vm.transformation.parameters);
	}

    /**
     * @ngdoc method
     * @name getClusterParams
     * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
     * @description [PRIVATE] Get cluster choice and choice parameters into one object
     * @returns {object} the parameters
     */
	function getClusterParams() {
		const params = {};
		if (vm.transformation.cluster) {
			_.chain(vm.transformation.cluster.clusters)
                .filter('active')
                .forEach((cluster) => {
	const replaceValue = cluster.replace.value;
	_.forEach(cluster.parameters, (param) => {
		if (param.value) {
			params[param.name] = replaceValue;
		}
	});
})
                .value();
		}

		return params;
	}

    /**
     * @ngdoc method
     * @name gatherParams
     * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
     * @description [PRIVATE] Gather params into one unique object
     * @returns {object} The entire parameter values
     */
	function gatherParams() {
		const params = getParams();
		const clusterParams = getClusterParams();
		return _.merge(params, clusterParams);
	}

    /**
     * @ngdoc method
     * @name transformWithParam
     * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
     * @description Gather params and perform a transformation on the column if the form is valid
     */
	vm.transformWithParam = function transformWithParam() {
		const transformationParams = gatherParams();
		vm.onSubmit({ params: transformationParams });
	};

    /**
     * @ngdoc method
     * @name submitHoverOn
     * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
     * @description Gather params and perform the submit mouseenter action
     */
	vm.submitHoverOn = function submitHoverOn() {
		vm.paramForm.$commitViewValue();
		const params = gatherParams();
		vm.onSubmitHoverOn({ params });
	};

    /**
     * @ngdoc method
     * @name submitHoverOff
     * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
     * @description Gather params and perform the submit mouseleave action
     */
	vm.submitHoverOff = function submitHoverOff() {
		const params = gatherParams();
		vm.onSubmitHoverOff({ params });
	};
}
