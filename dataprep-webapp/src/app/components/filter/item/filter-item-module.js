/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngTranslate from 'angular-translate';
import FILTER_ITEM_VALUE_MODULE from './value/filter-value-module';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';

import FilterItemCtrl from './filter-item-controller';
import FilterItemComponent from './filter-item-component';

const MODULE_NAME = 'data-prep.filter-item';

/**
 * @ngdoc object
 * @name data-prep.filter-item
 * @description This module contains the component to display filter item
 * @requires pascalprecht.translate
 * @requires talend.widget
 * @requires data-prep.services.filter
 */
angular
    .module(MODULE_NAME, [
	ngTranslate,
	FILTER_ITEM_VALUE_MODULE,
	TALEND_WIDGET_MODULE,
])
    .controller('FilterItemCtrl', FilterItemCtrl)
    .component('filterItem', FilterItemComponent);

export default MODULE_NAME;
