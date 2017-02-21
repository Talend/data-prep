/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_UTILS_MODULE from '../../../../services/utils/utils-module';

import LookupDatagridHeaderCtrl from './lookup-datagrid-header-controller';
import LookupDatagridHeader from './lookup-datagrid-header-directive';

const MODULE_NAME = 'data-prep.lookup-datagrid-header';

/**
 * @ngdoc object
 * @name data-prep.lookup-datagrid-header
 * @description This module contains the controller
 * and directives to manage the lookup-datagrid header with transformation menu
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		SERVICES_UTILS_MODULE,
	])
    .controller('LookupDatagridHeaderCtrl', LookupDatagridHeaderCtrl)
    .directive('lookupDatagridHeader', LookupDatagridHeader);

export default MODULE_NAME;
