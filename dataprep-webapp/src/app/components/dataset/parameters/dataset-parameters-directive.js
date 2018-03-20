/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './dataset-parameters.html';

/**
 * @ngdoc directive
 * @name data-prep.dataset-parameters.directive:datasetParameters
 * @description Dataset parameters edition form element
 * @restrict E
 * @usage
 *     <dataset-parameters
 *             processing="true"
 *             dataset="dataset"
 *             on-parameters-change="validate(dataset, parameters)"
 *             configuration="configuration"
 *             display-nb-lines="true"
 *             parameters="parameters"></dataset-parameters>
 * @param {boolean} processing Flag that indicate that the validation is processing.
 * It disables the validation button and switch to a loading icon.
 * @param {object} dataset The dataset we are editing
 * @param {function} onParametersChange The validation callback
 * @param {object} configuration The parameters configuration
 * {separators: [{label: ';', value: ';'}, ...], encodings: ['UTF-8', ...]}
 * @param {object} parameters The parameters values {separator: ';', encoding: 'UTF-8'}.
 * CAUTION: The values are directly bound to the inputs
 * @param {boolean} displayNbLines Display number of lines
 * */
export default function DatasetParameters() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			processing: '=',
			dataset: '=',
			onParametersChange: '&',
			configuration: '=',
			parameters: '=',
			displayNbLines: '<',
		},
		bindToController: true,
		controller: 'DatasetParametersCtrl',
		controllerAs: 'datasetParametersCtrl',
	};
}
