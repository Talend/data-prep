/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './filter-item.html';

/**
 * @ngdoc component
 * @name data-prep.filter-item.component:FilterItemComponent
 * @description Filter item represented by a badge with editable content
 * @usage
 <filter-item value="filter"
              editable="true|false"
              on-edit="editCallback()"
              removable="true|false"
              on-remove="removeCallback()"></filter-item>
 * @param {Object}    value      Object that contains the values in order to apply filter
 * @param {boolean}   editable   If render editable filter values
 * @param {function}  onEdit     The callback that is triggered on content edit
 * @param {boolean}   removable  If provide remove button to that filter item
 * @param {function}  onRemove   The callback that is triggered on badge close
 */
const FilterItemComponent = {
	templateUrl: template,
	controller: 'FilterItemCtrl',
	bindings: {
		value: '<',
		editable: '<',
		onEdit: '&',
		removable: '<',
		onRemove: '&',
	},
};

export default FilterItemComponent;
