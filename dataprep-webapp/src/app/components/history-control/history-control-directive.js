/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './history-control.html';

/**
 * @ngdoc directive
 * @name data-prep.history-control.directive:HistoryControl
 * @description This directive add the buttons and shortcuts to control the history actions
 * @restrict E
 */
export default function HistoryControl($document, HistoryService) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		controller() {
			this.service = HistoryService;
		},

		controllerAs: 'historyCtrl',
		link(scope) {
			function historyListener(event) {
                // CTRL+Z
				if (event.keyCode === 90 && event.ctrlKey) {
					HistoryService.undo();
				}
                // Ctrl+Y
				else if (event.keyCode === 89 && event.ctrlKey) {
					HistoryService.redo();
				}
			}

			$document.on('keydown', historyListener);

			scope.$on('$destroy', () => {
				$document.off('keydown', historyListener);
			});
		},
	};
}
