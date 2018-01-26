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
export default function ActionsSuggestions() {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		bindToController: true,
		controllerAs: 'actionsSuggestionsCtrl',
		controller(state, TransformationService) {
			'ngInject';
			this.TransformationService = TransformationService;
			this.state = state;
		},
	};
}
