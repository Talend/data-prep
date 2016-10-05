/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

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
					const splitHandler = angular.element('.split-handler').eq(0);
					const tabContainer = iElement.find('.action-suggestion-tab-items').eq(0);
					const elementToDisplay = tabContainer.find('sc-accordion-item > .sc-accordion.open').eq(0);
					const etdContainer = elementToDisplay.find('>.content-container').eq(0);
					if (!etdContainer.length) {
						return;
					}

					const tabOffset = tabContainer.offset();
					const etdOffset = etdContainer.offset();

					const etdHeight = etdContainer.height();

					const availableTopSpace = etdOffset.top - tabOffset.top;
					let scrollDistance;
					if (availableTopSpace >= etdHeight) {
						if (etdOffset.top > (splitHandler.offset().top - etdHeight)) {
							scrollDistance = tabContainer[0].scrollTop + etdHeight;
							tabContainer.animate({
								scrollTop: scrollDistance,
							}, 500);
						}
					}
					else {
						const accordionTriggerHeight = elementToDisplay.find('>.trigger-container').height();
						scrollDistance = (tabContainer[0].scrollTop + availableTopSpace) - accordionTriggerHeight;
						tabContainer.animate({
							scrollTop: scrollDistance,
						}, 500);
					}
				}, 300, false);
			};
		},
	};
}
