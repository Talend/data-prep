/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './confirm.html';

/**
 * @ngdoc directive
 * @name talend.widget.directive:TalendConfirm
 * @description This directive create a confirmation popup.<br/>
 * Key action (inherited from {@link talend.widget.directive:TalendModal TalendModal}):
 * <ul>
 *     <li>ENTER : validate (if not disabled)</li>
 *     <li>ESC : dismiss the modal</li>
 * </ul>
 * @restrict E
 * @usage
 <talend-confirm
     texts="texts">
 </talend-confirm>
 * @param {string[]} texts The texts ids (translation ids) to display
 */
export default function TalendConfirm() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			texts: '=',
		},
		bindToController: true,
		controller: 'TalendConfirmCtrl',
		controllerAs: 'confirmCtrl',
	};
}
