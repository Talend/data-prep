/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ABOUT_MODULE from '../about/about-module';
import DATASET_IMPORT_MODULE from '../dataset/import/dataset-import.module';
import DATASET_UPDLOAD_TILE_MODULE from '../dataset/upload-status/dataset-upload-status-module';
import DATASET_XLS_PREVIEW_MODULE from '../dataset/preview/dataset-xls-preview-module';
import FOLDER_CREATOR_MODULE from '../folder/creator/folder-creator-module';
import PREPARATION_CREATOR_MODULE from '../preparation/creator/preparation-creator-module';
import PREPARATION_COPY_MOVE_MODULE from '../preparation/copy-move/preparation-copy-move-module';
import STEP_PROGRESS_MODULE from '../step-progress/step-progress-module';
import SERVICES_DATASET_MODULE from '../../services/dataset/dataset-module';
import SERVICES_DATASET_WORKFLOW_MODULE from '../../services/dataset-workflow/dataset-workflow-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';
import SERVICES_UTILS_MODULE from '../../services/utils/utils-module';
import TALEND_WIDGET_MODULE from '../widgets/widget-module';
import WIDGETS_CONTAINERS_MODULE from '../widgets-containers/widgets-containers-module';

import HomeComponent from './home-component';
import HomeDatasetComponent from './dataset/home-dataset-container';
import HomePreparationComponent from './preparation/home-preparation-container';
import NameModalComponent from './name-modal/name-modal-component';

const MODULE_NAME = 'data-prep.home';

export default MODULE_NAME;

/**
 * @ngdoc object
 * @name data-prep.home
 * @description This module contains the home page of the app.
 * @requires talend.about
 * @requires talend.widget
 * @requires data-prep.dataset-import
 * @requires data-prep.dataset-upload-status
 * @requires data-prep.preparation-creator
 * @requires data-prep.preparation-copy-move
 * @requires data-prep.step-progress
 * @requires data-prep.services.dataset
 * @requires data-prep.services.datasetWorkflowService
 * @requires data-prep.services.state
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		ABOUT_MODULE,
		DATASET_UPDLOAD_TILE_MODULE,
		DATASET_IMPORT_MODULE,
		DATASET_XLS_PREVIEW_MODULE,
		FOLDER_CREATOR_MODULE,
		PREPARATION_CREATOR_MODULE,
		PREPARATION_COPY_MOVE_MODULE,
		TALEND_WIDGET_MODULE,
		WIDGETS_CONTAINERS_MODULE,
		STEP_PROGRESS_MODULE,

		SERVICES_DATASET_MODULE,
		SERVICES_DATASET_WORKFLOW_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
	.component('home', HomeComponent)
	.component('homeDataset', HomeDatasetComponent)
	.component('homePreparation', HomePreparationComponent)
	.component('nameModal', NameModalComponent);
