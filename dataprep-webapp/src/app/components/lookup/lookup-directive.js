/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './lookup.html';

/**
 * @ngdoc directive
 * @name data-prep.lookup.directive:Lookup
 * @description This directive displays the lookup window
 * @restrict E
 * @usage <lookup visible="visible"></lookup>
 * @param {Boolean} visible boolean to show or hide the lookup
 */
export default function Lookup() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			visible: '=',
		},
		bindToController: true,
		controllerAs: 'lookupCtrl',
		controller: 'LookupCtrl',
	};
}
