/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './filter-search.html';

/**
 * @ngdoc directive
 * @name data-prep.filter-search.directive:FilterSearch
 * @description This directive create an input to add a filter. The `keydown` event is stopped to avoid propagation
 * to a possible {@link talend.widget.directive:TalendModal TalendModal} container
 * @restrict E
 */
export default function FilterSearch() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {},
		bindToController: true,
		controllerAs: 'filterCtrl',
		controller: 'FilterSearchCtrl',
		link: (scope, iElement, attrs, ctrl) => {
			iElement.bind('keydown', (e) => {
				if (e.keyCode === 27) {
					e.stopPropagation();
				}
			});

			const inputElement = iElement.find('input');
			inputElement[0].onblur = () => {
				ctrl.filterSearch = '';
			};
		},
	};
}
