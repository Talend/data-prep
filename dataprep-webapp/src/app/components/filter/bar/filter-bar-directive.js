/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './filter-bar.html';

export default function FilterBar(state, FilterManagerService) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		scope: {},
		bindToController: true,
		controller() {
			this.filterManagerService = FilterManagerService;
			this.state = state;
		},

		controllerAs: 'filterBarCtrl',
	};
}
