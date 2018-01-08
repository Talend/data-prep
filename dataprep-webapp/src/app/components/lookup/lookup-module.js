/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import DATAGRID_MODULE from '../datagrid/datagrid-module';
import INVENTORY_HEADER_MODULE from '../inventory/header/inventory-header-module';
import LOOKUP_DATAGRID_HEADER_MODULE from './grid/header/lookup-datagrid-header-module';
import TALEND_WIDGET_MODULE from '../widgets/widget-module';
import SERVICES_EARLY_PREVIEW_MODULE from '../../services/early-preview/early-preview-module';
import SERVICES_DATASET_MODULE from '../../services/dataset/dataset-module';
import SERVICES_LOOKUP_MODULE from '../../services/lookup/lookup-module';
import SERVICES_PREVIEW_MODULE from '../../services/preview/preview-module';
import SERVICES_PLAYGROUND_MODULE from '../../services/playground/playground-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';
import SERVICES_STATISTICS_MODULE from '../../services/statistics/statistics-module';
import SERVICES_UTILS_MODULE from '../../services/utils/utils-module';

import LookupCtrl from './lookup-controller';
import Lookup from './lookup-directive';

import LookupDatagridColumnService from './grid/services/lookup-datagrid-column-service';
import LookupDatagridGridService from './grid/services/lookup-datagrid-grid-service';
import LookupDatagridStyleService from './grid/services/lookup-datagrid-style-service';
import LookupDatagrid from './grid/lookup-datagrid-directive';
import LookupDatasetList from './dataset/list/lookup-dataset-list-component';

const MODULE_NAME = 'data-prep.lookup';

/**
 * @ngdoc object
 * @name data-prep.lookup
 * @description This module contains the dataset lookup
 * @requires talend.widget
 * @requires data-prep.lookup-datagrid-header
 * @requires data-prep.services.dataset
 * @requires data-prep.services.early-preview
 * @requires data-prep.services.lookup
 * @requires data-prep.services.playground
 * @requires data-prep.services.preview
 * @requires data-prep.services.state
 * @requires data-prep.services.statistics
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		DATAGRID_MODULE,
		INVENTORY_HEADER_MODULE,
		LOOKUP_DATAGRID_HEADER_MODULE,
		TALEND_WIDGET_MODULE,
		SERVICES_DATASET_MODULE,
		SERVICES_EARLY_PREVIEW_MODULE,
		SERVICES_LOOKUP_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_PREVIEW_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_STATISTICS_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .controller('LookupCtrl', LookupCtrl)
    .directive('lookup', Lookup)

    .service('LookupDatagridColumnService', LookupDatagridColumnService)
    .service('LookupDatagridGridService', LookupDatagridGridService)
    .service('LookupDatagridStyleService', LookupDatagridStyleService)
    .directive('lookupDatagrid', LookupDatagrid)
    .component('lookupDatasetList', LookupDatasetList);

export default MODULE_NAME;
