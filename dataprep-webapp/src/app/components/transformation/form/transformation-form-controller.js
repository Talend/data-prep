/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import _ from 'lodash';

/**
 * @ngdoc controller
 * @name data-prep.transformation-form.controller:TransformFormCtrl
 * @description Transformation parameters controller.
 */
export default class TransformFormCtrl {
	constructor(TransformationUtilsService) {
		'ngInject';
		this.TransformationUtilsService = TransformationUtilsService;
	}

	/**
	 * @ngdoc method
	 * @name getParams
	 * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
	 * @description [PRIVATE] Get item parameters into one object for REST call
	 * @returns {object} the parameters
	 */
	getParams() {
		return this.TransformationUtilsService.extractParams(
			{},
			this.transformation.parameters
		);
	}

	/**
	 * @ngdoc method
	 * @name getClusterParams
	 * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
	 * @description [PRIVATE] Get cluster choice and choice parameters into one object
	 * @returns {object} the parameters
	 */
	getClusterParams() {
		const params = {};
		if (this.transformation.cluster) {
			this.transformation.cluster.clusters
				.filter(cluster => cluster.active)
				.forEach((cluster) => {
					const replaceValue = cluster.replace.value;
					cluster.parameters
						.filter(param => param.value)
						.forEach((param) => {
							params[param.name] = replaceValue;
						});
				});
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
	gatherParams() {
		const params = this.getParams();
		const clusterParams = this.getClusterParams();
		return _.merge(params, clusterParams);
	}

	/**
	 * @ngdoc method
	 * @name transformWithParam
	 * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
	 * @description Gather params and perform a transformation on the column if the form is valid
	 */
	transformWithParam() {
		const transformationParams = this.gatherParams();
		this.onSubmit({ params: transformationParams });
	}

	/**
	 * @ngdoc method
	 * @name submitHoverOn
	 * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
	 * @description Gather params and perform the submit mouseenter action
	 */
	submitHoverOn() {
		this.paramForm.$commitViewValue();
		const params = this.gatherParams();
		this.onSubmitHoverOn({ params });
	}

	/**
	 * @ngdoc method
	 * @name submitHoverOff
	 * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
	 * @description Gather params and perform the submit mouseleave action
	 */
	submitHoverOff() {
		const params = this.gatherParams();
		this.onSubmitHoverOff({ params });
	}
}
