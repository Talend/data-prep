/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import DATAGRID_HEADER_MODULE from './header/datagrid-header-module';
import DATAGRID_INDEX_HEADER_MODULE from './index-header/datagrid-index-header-module';
import SERVICES_DATEGRID_MODULE from '../../services/datagrid/datagrid-module';
import SERVICES_PLAYGROUND_MODULE from '../../services/playground/playground-module';
import SERVICES_PREVIEW_MODULE from '../../services/preview/preview-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';
import SERVICES_STATISTICS_MODULE from '../../services/statistics/statistics-module';
import SERVICES_UTILS_MODULE from '../../services/utils/utils-module';

import TalendEditor from './editor/talend-editor';
import DatagridColumnService from './services/datagrid-column-service';
import DatagridExternalService from './services/datagrid-external-service';
import DatagridGridService from './services/datagrid-grid-service';
import DatagridSizeService from './services/datagrid-size-service';
import DatagridStyleService from './services/datagrid-style-service';
import DatagridTooltipService from './services/datagrid-tooltip-service';
import Datagrid from './datagrid-directive';

import DatagridMessageComponent from './message/datagrid-message-compnent';

Slick.Editors.TalendEditor = TalendEditor;
/**
 * @ngdoc object
 * @name data-prep.datagrid
 * @description This module contains the controller and directives for the datagrid
 * @requires data-prep.datagrid-header
 * @requires data-prep.datagrid-index-header
 * @requires data-prep.services.datagrid
 * @requires data-prep.services.playground
 * @requires data-prep.services.preview
 * @requires data-prep.services.state
 * @requires data-prep.services.statistics
 * @requires data-prep.services.utils
 */

const MODULE_NAME = 'data-prep.datagrid';
angular.module(MODULE_NAME,
	[
		DATAGRID_HEADER_MODULE,
		DATAGRID_INDEX_HEADER_MODULE,
		SERVICES_DATEGRID_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_PREVIEW_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_STATISTICS_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('DatagridColumnService', DatagridColumnService)
    .service('DatagridExternalService', DatagridExternalService)
    .service('DatagridGridService', DatagridGridService)
    .service('DatagridSizeService', DatagridSizeService)
    .service('DatagridStyleService', DatagridStyleService)
    .service('DatagridTooltipService', DatagridTooltipService)
    .directive('datagrid', Datagrid)
    .component('datagridMessage', DatagridMessageComponent);

export default MODULE_NAME;
