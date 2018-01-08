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

import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import TRANSFORMATION_MENU_MODULE from '../../transformation/menu/transformation-menu-module';

import SERVICES_COLUMN_TYPES_MODULE from '../../../services/column-types/column-types-module';
import SERVICES_FILTER_MANAGER_MODULE from '../../../services/filter/manager/filter-manager-module';
import SERVICES_PLAYGROUND_MODULE from '../../../services/playground/playground-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';
import SERVICES_TRANSFORMATION_MODULE from '../../../services/transformation/transformation-module';
import SERVICES_UTILS_MODULE from '../../../services/utils/utils-module';

import DatagridHeaderCtrl from './datagrid-header-controller';
import DatagridHeader from './datagrid-header-directive';

const MODULE_NAME = 'data-prep.datagrid-header';

/**
 * @ngdoc object
 * @name data-prep.datagrid-header
 * @description This module contains the entitues to manage the datagrid header
 * @requires talend.module.widget
 * @requires data-prep.transformation-menu
 * @requires data-prep.services.utils
 * @requires data-prep.services.playground
 * @requires data-prep.services.filter-manager
 * @requires data-prep.services.transformation
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		sunchoke.dropdown,
		TALEND_WIDGET_MODULE,
		TRANSFORMATION_MENU_MODULE,
		SERVICES_COLUMN_TYPES_MODULE,
		SERVICES_FILTER_MANAGER_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_TRANSFORMATION_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .controller('DatagridHeaderCtrl', DatagridHeaderCtrl)
    .directive('datagridHeader', DatagridHeader);

export default MODULE_NAME;
