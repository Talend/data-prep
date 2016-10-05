/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngTranslate from 'angular-translate';
import uiRouter from 'angular-ui-router';
import DATASET_XLS_PREVIEW_MODULE from '../preview/dataset-xls-preview-module';
import INVENTORY_COPY_MOVE_MODULE from '../../inventory/copy-move/inventory-copy-move-module';
import INVENTORY_ITEM_MODULE from '../../inventory/item/inventory-item-module';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_DATASET_WORKFLOW_MODULE from '../../../services/dataset-workflow/dataset-workflow-module';
import SERVICES_DATASET_MODULE from '../../../services/dataset/dataset-module';
import SERVICES_FOLDER_MODULE from '../../../services/folder/folder-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';
import SERVICES_UTILS_MODULE from '../../../services/utils/utils-module';

import DatasetList from './dataset-list-component';

const MODULE_NAME = 'data-prep.dataset-list';

/**
 * @ngdoc object
 * @name data-prep.dataset-list
 * @description This module contains the controller and directives to manage the dataset list
 * @requires talend.widget
 * @requires data-prep.dataset-xls-preview
 * @requires data-prep.inventory-copy-move
 * @requires data-prep.inventory-item
 * @requires data-prep.services.dataset
 * @requires data-prep.services.datasetWorkflowService
 * @requires data-prep.services.folder
 * @requires data-prep.services.state
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		ngTranslate,
		uiRouter,
		DATASET_XLS_PREVIEW_MODULE,
		INVENTORY_COPY_MOVE_MODULE,
		INVENTORY_ITEM_MODULE,
		TALEND_WIDGET_MODULE,
		SERVICES_DATASET_MODULE,
		SERVICES_DATASET_WORKFLOW_MODULE,
		SERVICES_FOLDER_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .component('datasetList', DatasetList);

export default MODULE_NAME;
