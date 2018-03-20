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
 * @name talend.widget.directive:TalendSearchInput
 * @description This directive create a search input
 * @restrict E
 * @usage <talend-search-input ng-model="model"></talend-search-input>
 */
export default function TalendSearchInput() {
	return {
		restrict: 'A',
		require: 'ngModel',
		link(scope, iElement, iAttrs, ngModel) {
			// var margin = 5;
			const wrapper = angular.element('<div class="search-input"></div>');
			iElement.wrap(wrapper);

			const clearButton = angular.element('<div class="search-input-icon clear-icon"><span class="icon" data-icon="d"></span></div>');
			const searchIcon = angular.element('<div class="search-input-icon"><span class="icon" data-icon="D"></span></div>');

			iElement.parent().append(clearButton);
			iElement.parent().append(searchIcon);

			scope.$watch(
				function () {
					return ngModel.$modelValue;
				},

				function (value) {
					if (value) {
						clearButton.css('display', 'block');
						searchIcon.css('display', 'none');
					}
					else {
						searchIcon.css('display', 'block');
						clearButton.css('display', 'none');
					}
				});

			clearButton.on('click', function () {
				scope.$apply(function () {
					ngModel.$setViewValue('');
					ngModel.$render();
				});
			});
		},
	};
}
