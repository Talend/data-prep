/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './button-loader.html';

/**
 * @ngdoc directive
 * @name talend.widget.directive:TalendButtonLoader
 * @description This directive create a button that switch content to a loading icon
 * @restrict E
 * @usage
 <talend-button-loader
     button-class=""
     disable-condition=""
     loading=""
     loading-class="">
 </talend-button-loader>

 * @param {string} button-class The css class to add to the button
 * @param {boolean} disable-condition The condition to disable the button
 * @param {boolean} loading The loading flag
 * @param {string} loading-class The class to add to the loader
 */
export default function TalendButtonLoader() {
	return {
		restrict: 'E',
		templateUrl: template,
		transclude: true,
		scope: {
			buttonClass: '@',
			disableCondition: '=',
			loading: '=',
			loadingClass: '@',
		},
		bindToController: true,
		controller: () => {
		},

		controllerAs: 'buttonLoaderCtrl',
	};
}
