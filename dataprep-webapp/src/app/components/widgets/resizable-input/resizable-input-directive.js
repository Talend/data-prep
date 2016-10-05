/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

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
 resizable-input
 resizable-input-offset="10" />
 */
const InputResizable = () => {
	return {
		restrict: 'A',
		scope: {
			resizableInputOffset: '<',
		},
		require: 'ngModel',
		link: (scope, element, attrs, ngModel) => {
			/**
			 * @type {number} Minimum input width in px
			 */
			const minInputWidth = 30;

			/**
			 * Adjust input width
			 */
			function updateSize() {
				const input = element;
				const inputValue = input.val();
				if (!inputValue || !inputValue.length) {
					return;
				}

				const resizableInputOffset = scope.resizableInputOffset;
				const inputWidth = ((inputValue.length + 1 + ((inputValue.split('\t').length - 1) * 8)) * 7) + (resizableInputOffset);
				input.css('width', (inputWidth < minInputWidth ? minInputWidth : inputWidth) + 'px');
			}

			scope.$watchGroup(
				[
					() => ngModel.$modelValue,
					() => scope.resizableInputOffset,
				],
				updateSize
			);
		},
	};
};

export default InputResizable;
