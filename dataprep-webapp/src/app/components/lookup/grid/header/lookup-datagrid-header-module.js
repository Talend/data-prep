/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import TRANSFORMATION_MENU_MODULE from '../../../transformation/menu/transformation-menu-module';
import TALEND_WIDGET_MODULE from '../../../widgets/widget-module';
import SERVICES_PLAYGROUND_MODULE from '../../../../services/playground/playground-module';
import SERVICES_TRANSFORMATION_MODULE from '../../../../services/transformation/transformation-module';
import SERVICES_UTILS_MODULE from '../../../../services/utils/utils-module';

import LookupDatagridHeaderCtrl from './lookup-datagrid-header-controller';
import LookupDatagridHeader from './lookup-datagrid-header-directive';

const MODULE_NAME = 'data-prep.lookup-datagrid-header';

/**
 * @ngdoc object
 * @name data-prep.lookup-datagrid-header
 * @description This module contains the controller
 * and directives to manage the lookup-datagrid header with transformation menu
 * @requires talend.widget
 * @requires data-prep.transformation-menu
 * @requires data-prep.services.utils
 * @requires data-prep.services.playground
 * @requires data-prep.services.transformation
 */
angular.module(MODULE_NAME,
	[
		TALEND_WIDGET_MODULE,
		TRANSFORMATION_MENU_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_TRANSFORMATION_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .controller('LookupDatagridHeaderCtrl', LookupDatagridHeaderCtrl)
    .directive('lookupDatagridHeader', LookupDatagridHeader);

export default MODULE_NAME;
