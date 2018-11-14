/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './feedback.html';

/**
 * @ngdoc directive
 * @name data-prep.datagrid.directive:Feedback
 * @description This directive create a feedback form
 * @restrict E
 * @usage <feedback></feedback>
 */
export default function Feedback() {
	return {
		templateUrl: template,
		restrict: 'E',
		bindToController: true,
		controllerAs: 'feedbackCtrl',
		controller: 'FeedbackCtrl',
	};
}
