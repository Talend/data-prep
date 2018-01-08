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
import 'angular-mass-autocomplete';
import SERVICES_DATAGRID_MODULE from '../../../services/datagrid/datagrid-module';
import SERVICES_FILTER_MANAGER_MODULE from '../../../services/filter/manager/filter-manager-module';

import FilterSearchCtrl from './filter-search-controller';
import FilterSearch from './filter-search-directive';

const MODULE_NAME = 'data-prep.filter-search';

/**
 * @ngdoc object
 * @name data-prep.filter-search
 * @description This module contains the entities to manage the filter search input
 * as the user type in the input.
 * @requires data-prep.services.filter
 * @requires data-prep.services.datagrid
 */
angular.module(MODULE_NAME,
	[
		'MassAutoComplete',
		ngTranslate,
		SERVICES_DATAGRID_MODULE,
		SERVICES_FILTER_MANAGER_MODULE,
	])
    .controller('FilterSearchCtrl', FilterSearchCtrl)
    .directive('filterSearch', FilterSearch);

export default MODULE_NAME;
