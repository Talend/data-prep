/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './actions-suggestions.html';

/**
 * @ngdoc directive
 * @name data-prep.actions-suggestions.directive:actionsSuggestions
 * @description Actions and suggestions list element
 * @restrict E
 * @usage <actions-suggestions></actions-suggestions>
 * */
export default function ActionsSuggestions($timeout) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		bindToController: true,
		controllerAs: 'actionsSuggestionsCtrl',
		controller: 'ActionsSuggestionsCtrl',
		link: (scope, iElement, iAttrs, ctrl) => {
            // Scroll the actual tab container to the bottom of the element to display
			ctrl.scrollToBottom = function scrollToBottom() {
				$timeout(function () {
					const tabContainer = iElement.find('.action-suggestion-tab-items').eq(0);
					const elementToDisplay = tabContainer.find('sc-accordion-item > .sc-accordion.open').eq(0);
					tabContainer.animate({
						scrollTop: elementToDisplay.position().top,
					}, 500);
				}, 300, false);
			};
		},
	};
}
