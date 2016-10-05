/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
/**
 * @ngdoc component
 * @name data-prep.breadcrumb.component:BreadcrumbComponent
 * @description This component display a breadcrumb
 * @restrict E
 *
 * @usage
 * <breadcrumb children="$ctrl.state.inventory.breadcrumbChildren"
 *             items="$ctrl.state.inventory.breadcrumb"
 *             on-list-open="$ctrl.fetchChildren(item)"
 *             on-select="$ctrl.go(item)">
 * </breadcrumb>
 *
 * @param {array}       children items of the dropdown of a node of the breadcrumbChildren
 * @param {array}       items nodes of the breadcrumb
 * @param {function}    onListOpen function to call when opening a dropdown
 * @param {function}    onSelect function to call when selecting an item of the breadcrumb
 */

import template from './breadcrumb.html';

const BreadcrumbComponent = {
	templateUrl: template,
	bindings: {
		children: '<',
		items: '<',
		onListOpen: '&',
		onSelect: '&',
	},
};

export default BreadcrumbComponent;
