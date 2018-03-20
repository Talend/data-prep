/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './transformation-cluster-params.html';

/**
 * @ngdoc directive
 * @name data-prep.transformation-form.directive:TransformClusterParams
 * @description This directive display a transformation cluster parameters form.
 * @restrict E
 * @usage <transform-cluster-params details="parameters"></transform-cluster-params>
 * @param {object} details The transformation cluster parameters details
 */
export default function TransformClusterParams($timeout) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			details: '=',
			isReadonly: '<',
		},
		bindToController: true,
		controllerAs: 'clusterParamsCtrl',
		controller: 'TransformClusterParamsCtrl',
		link(scope, iElement, iAttrs, ctrl) {
			/**
			 * @ngdoc property
			 * @name allActivationCheckboxes
			 * @propertyOf data-prep.transformation-form.directive:TransformClusterParams
			 * @description [PRIVATE] Each element contains
			 */
			const allActivationCheckboxes = [];

			/**
			 * @ngdoc method
			 * @name updateStyles
			 * @methodOf data-prep.transformation-form.directive:TransformClusterParams
			 * @description [PRIVATE] Refresh the cluster styles with the new provided active flags
			 */
			function updateStyles(activationValues) {
				_.chain(allActivationCheckboxes)
					.zip(activationValues)
					.forEach((zipItem) => {
						const item = zipItem[0];
						const checked = zipItem[1];

						if (item.lastState !== checked) {
							if (checked) {
								item.row.removeClass('disabled');
							}
							else {
								item.row.addClass('disabled');
							}

							item.inputs.prop('disabled', ctrl.isReadonly || !checked);
							item.selects.prop('disabled', !checked);
							item.lastState = checked;
						}
					})
					.value();
			}

			$timeout(() => {
				const clustersRows = iElement.find('.cluster-body >.cluster-line');

				// attach change listener on each row enable/disable checkbox
				clustersRows.each((index) => {
					const row = clustersRows.eq(index);
					const rowInputs = row.find('input:not(.cluster-activation)');
					const rowSelects = row.find('select');
					const checkbox = row.find('>div:first >input.cluster-activation');

					allActivationCheckboxes[index] = {
						checkbox,
						row,
						inputs: rowInputs,
						selects: rowSelects,
					};
				});

				// refresh style on cluster active flag change
				scope.$watchCollection(
					() => _.map(ctrl.details.clusters, 'active'),
					(activationValues) => {
						ctrl.refreshToggleCheckbox();
						updateStyles(activationValues);
					}
				);
			}, 0, false);
		},
	};
}
