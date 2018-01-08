/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './transformation-form.html';

/**
 * @ngdoc directive
 * @name data-prep.transformation-form.directive:TransformForm
 * @description This directive display a transformation parameters form
 * @restrict E
 * @usage
 <transform-form
         transformation="transformation"
         on-submit="callback()"
         on-submit-hover-on="callbackOn()"
         on-submit-hover-off="callbackOff()"
         is-transformation-in-progress="true">
 </transform-form>
 * @param {object} transformation The transformation containing parameters
 * @param {function} onSubmit The callback executed on form submit
 * @param {function} onSubmitHoverOn The callback executed on mouseenter on form submit
 * @param {function} onSubmitHoverOff The callback executed on mouseleave on form submit
 * @param {boolean} isTransformationInProgress The flag indicate whether the transformation is in progress
 */
export default function TransformForm() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			transformation: '=',
			onSubmit: '&',
			onSubmitHoverOn: '&',
			onSubmitHoverOff: '&',
			isTransformationInProgress: '<',
			isReadonly: '<',
		},
		bindToController: true,
		controllerAs: 'formCtrl',
		controller: 'TransformFormCtrl',
	};
}
