/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngSanitize from 'angular-sanitize';

import FilterValueCtrl from './filter-value-controller';
import FilterValueComponent from './filter-value-component';

import SERVICES_UTILS_MODULE from '../../../../services/utils/utils-module';

const MODULE_NAME = 'data-prep.filter-item-value';

/**
 * @ngdoc object
 * @name data-prep.filter-item-value
 * @description This module contains the component to display filter item value
 */
angular
	.module(MODULE_NAME,
	[
		ngSanitize,
		SERVICES_UTILS_MODULE,
	])
	.controller('FilterValueCtrl', FilterValueCtrl)
	.component('filterValue', FilterValueComponent);

export default MODULE_NAME;
