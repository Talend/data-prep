/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import _ from 'lodash';

const CHOICE_TYPE = 'CHOICE';
const CLUSTER_TYPE = 'CLUSTER';

/**
 * @ngdoc service
 * @name data-prep.services.parameters.service:ParametersService
 * @description Manage dynamic parameters
 */
export default class ParametersService {
	constructor(ConverterService) {
		'ngInject';
		this.ConverterService = ConverterService;
	}

    /**
     * @ngdoc method
     * @name resetParamValue
     * @methodOf data-prep.services.parameters.service:ParametersService
     * @param {object} params The params to reset
     * @param {string} type The param type
     * @description Reset params values with saved initial values
     */
	resetParamValue(params, type) {
		if (!params) {
			return;
		}

		function executeOnSimpleParams(simpleParamsToInit) {
			_.forEach(simpleParamsToInit, (param) => {
				param.value = angular.isDefined(param.initialValue) ?
                    param.initialValue :
                    param.default;
			});
		}

		switch (type) {
		case CHOICE_TYPE:
			_.forEach(params, (choice) => {
				choice.selectedValue = angular.isDefined(choice.initialValue) ?
                        choice.initialValue :
                        choice.default;

				_.forEach(choice.values, (choiceItem) => {
					executeOnSimpleParams(choiceItem.parameters);
				});
			});
			break;

		case CLUSTER_TYPE:
			_.forEach(params.clusters, (cluster) => {
				cluster.active = cluster.initialActive;
				executeOnSimpleParams(cluster.parameters);
				executeOnSimpleParams([cluster.replace]);
			});
			break;

		default:
			executeOnSimpleParams(params);
		}
	}

    /**
     * @ngdoc method
     * @name initParameters
     * @methodOf data-prep.services.parameters.service:ParametersService
     * @param {object} parameters The parameters
     * @param {object} paramValues The parameters initial values
     * @description Init parameters initial value and type
     * @returns {object[]} The parameters with initialized values
     */
	initParameters(parameters, paramValues) {
		return _.chain(parameters)
            .filter(param => !param.implicit)
            .forEach((param) => {
	param.initialValue = param.value = this.ConverterService.adaptValue(
                    param.type,
                    paramValues[param.name]
                );
	param.inputType = this.ConverterService.toInputType(param.type);

                // also take care of select parameters
	if (param.type === 'select' && param.configuration && param.configuration.values) {
		_.forEach(param.configuration.values, (selectItem) => {
			this.initParameters(selectItem.parameters, paramValues);
		});
	}
})
            .value();
	}

    /**
     * @ngdoc method
     * @name initCluster
     * @methodOf data-prep.services.parameters.service:ParametersService
     * @param {object} cluster The Cluster parameters
     * @param {object} paramValues The clusters initial values
     * @description Init Clusters initial value
     * @returns {object} The Cluster with initialized values
     */
	initCluster(cluster, paramValues) {
		_.forEach(cluster.clusters, (clusterItem) => {
			const firstActiveParam = _.chain(clusterItem.parameters)
                .forEach((param) => {
	param.initialValue = param.value = param.name in paramValues;
})
                .filter('value')
                .first()
                .value();
			clusterItem.initialActive = !!firstActiveParam;

            // get the replace value or the default if the cluster item is inactive
            // and init the replace input value
			const replaceValue = firstActiveParam ?
                paramValues[firstActiveParam.name] :
                clusterItem.replace.default;
			const replaceParamValues = { replaceValue };
			this.initParameters([clusterItem.replace], replaceParamValues);
		});
		return cluster;
	}

    /**
     * @ngdoc method
     * @name initParamsValues
     * @methodOf data-prep.services.parameters.service:ParametersService
     * @param {object} transformation The transformation infos
     * @param {object} paramValues The transformation parameters initial values
     * @description Init parameters values and save them as initial values
     */
	initParamsValues(transformation, paramValues) {
		if (transformation.parameters) {
			transformation.parameters = this.initParameters(transformation.parameters, paramValues);
		}

		if (transformation.cluster) {
			transformation.cluster = this.initCluster(transformation.cluster, paramValues);
		}
	}

    /**
     * @ngdoc method
     * @name resetParameters
     * @methodOf data-prep.services.parameters.service:ParametersService
     * @description Reset all the transformation parameters
     */
	resetParameters(transformation) {
		transformation.parameters = null;
		transformation.cluster = null;
	}
}
