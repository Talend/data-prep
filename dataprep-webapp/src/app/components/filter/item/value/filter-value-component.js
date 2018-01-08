/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './filter-value.html';

/**
 * @ngdoc component
 * @name data-prep.filter-item-value.component:FilterValueComponent
 * @description Filter item value represented by an input with editable content or a span
 * @usage
 <filter-value value="filterValue"
               editable="true|false"
               on-edit="editCallback()"
               removable="true|false"
               on-remove="removeCallback()"></filter-value>
 * @param {Object}     value         Filter value to display
 * @param {boolean}    editable      If render editable filter values
 * @param {Function}   onEdit        Callback when input has changed
 * @param {boolean}    removable     If provide remove button to that filter value
 * @param {Function}   onRemove      Callback when value has been removed
 */
const FilterValueComponent = {
	templateUrl: template,
	controller: 'FilterValueCtrl',
	bindings: {
		value: '<',
		editable: '<',
		onEdit: '&',
		removable: '<',
		onRemove: '&',
	},
};

export default FilterValueComponent;
