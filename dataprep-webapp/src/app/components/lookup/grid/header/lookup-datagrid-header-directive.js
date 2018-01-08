/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './lookup-datagrid-header.html';

/**
 * @ngdoc directive
 * @name data-prep.lookup-datagrid-header.directive:DatagridHeader
 * @description This directive creates the lookup datagrid column header
 * @restrict E
 * @usage
 <lookup-datagrid-header
         added="added"
         column="column">
 </lookup-datagrid-header>
 * @param {object} column The column metadata
 * @param {object} added checkbox ng-model
 */
export default function LookupDatagridHeader($timeout) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			column: '=',
			added: '=',
		},
		bindToController: true,
		controllerAs: 'lookupDatagridHeaderCtrl',
		controller: 'LookupDatagridHeaderCtrl',
		link: (scope, iElement) => {
			let addToLookupDiv;
			let addToLookupCheckbox;
			$timeout(() => {
				addToLookupDiv = iElement.find('.add-to-lookup');
				addToLookupDiv.on('click', (e) => {
					e.stopPropagation();
					addToLookupCheckbox = addToLookupDiv.find('input[type=checkbox]');
					if (addToLookupCheckbox) {
						addToLookupCheckbox.click();
					}
				});
			}, 250, false);
		},
	};
}
