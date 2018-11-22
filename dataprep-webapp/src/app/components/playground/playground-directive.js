/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './playground.html';

const ESC = 27;
const Y = 89;
const Z = 90;

/**
 * @ngdoc directive
 * @name data-prep.playground.directive:Playground
 * @description This directive create the playground.
 * @restrict E
 */
export default function Playground($timeout, HistoryService) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		bindToController: true,
		controllerAs: 'playgroundCtrl',
		controller: 'PlaygroundCtrl',
		link: (scope, iElement, iAttrs, ctrl) => {
			const container = iElement.find('.playground-container').eq(0);

			container.bind('keydown', (e) => {
				if (e.keyCode === ESC) {
					if (ctrl.state.playground.nameEditionMode) {
						container.focus();
					}
					else {
						$timeout(ctrl.beforeClose);
					}
				}
				else if (e.keyCode === Z && e.ctrlKey) {
					HistoryService.undo();
				}
				else if (e.keyCode === Y && e.ctrlKey) {
					HistoryService.redo();
				}
			});

			container.focus();
		},
	};
}
