/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc directive
 * @name talend.widget.directive:InputResizable
 * @description This directive create an resizable input.<br/>
 * Watchers :
 * <ul>
 *     <li>on ng-model change, the input size is recalculated</li>
 *     <li>if input becomes removable ng-model change, the input size is recalculated</li>
 * </ul>
 * @restrict A
 * @usage
 <input ng-model="ngModel"
 resizable-input />
 */

const AVERAGE_CHAR_WIDTH = 0.5;
const MINIMUM_WIDTH = 1;

const InputResizable = () => {
	return {
		restrict: 'A',
		require: 'ngModel',
		link: (scope, element, attrs, ngModel) => {
			/**
			 * Adjust input width
			 */
			function updateSize() {
				const input = element;
				const length = input.val().length;
				const width = Math.max(length * AVERAGE_CHAR_WIDTH, MINIMUM_WIDTH);
				input.css('width', width + 'em');
			}

			scope.$watchGroup([() => ngModel.$modelValue], updateSize);
		},
	};
};

export default InputResizable;
