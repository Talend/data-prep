/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import sunchoke from 'sunchoke';
import SERVICES_FILTER_MANAGER_MODULE from '../../../services/filter/manager/filter-manager-module';
import DatagridIndexHeader from './datagrid-index-header-component';

const MODULE_NAME = 'data-prep.datagrid-index-header';

/**
 * @ngdoc object
 * @name data-prep.datagrid-index-header
 * @description This module contains the entitues to manage the datagrid header of the index column
 * @requires talend.module.widget
 * @requires data-prep.services.filter
 */
angular.module(MODULE_NAME,
	[
		sunchoke.dropdown,
		SERVICES_FILTER_MANAGER_MODULE,
	])
    .component('datagridIndexHeader', DatagridIndexHeader);

export default MODULE_NAME;
