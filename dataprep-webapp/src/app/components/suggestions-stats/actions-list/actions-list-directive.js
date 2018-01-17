/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './actions-list.html';

/**
 * @ngdoc directive
 * @name data-prep.actions-list.directive:actionsList
 * @description list of the action to apply on a (column, cell or dataset)
 * @restrict E
 * @usage
 *     <action-list
 *             actions="actions"
 *             should-render-category="shouldRenderCategoryCb"
 *             should-render-action="shouldRenderActionCb"
 *             scroll-to-bottom="scrollFn"
 *             scope="scope"></action-list>
 * @param {array} actions The actions to render
 * @param {function} shouldRenderCategory Function that define if the category passed as parameter should be rendered
 * @param {function} shouldRenderActionCb Function that define if the action passed as parameter should be rendered
 * @param {function} scrollToBottom The scroll action when an action is opened
 * @param {string} scope The action scope (LINE, COLUMN, ...)
 * */
export default function ActionsList() {
	return {
		restrict: 'E',
		templateUrl: template,
		bindToController: true,
		controllerAs: 'actionsListCtrl',
		controller: 'ActionsListCtrl',
		scope: {
			actions: '=',
			shouldRenderCategory: '=',
			shouldRenderAction: '=',
			scrollToBottom: '=',
			scope: '@',
		},
		link: {
			post(scope, iElement, iAttrs, ctrl) {
				scope.$watch(
					() => ctrl.actions,
					(newValue) => {
						if (newValue) {
							ctrl.cancelEarlyPreview();
						}
					}, true);
			},
		},
	};
}
