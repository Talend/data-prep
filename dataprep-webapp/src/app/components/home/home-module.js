/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import DATASET_HEADER_MODULE from '../dataset/header/dataset-header-module';
import DATASET_LIST_MODULE from '../dataset/list/dataset-list-module';
import DATASET_UPDLOAD_LIST_MODULE from '../dataset/upload-list/dataset-upload-list-module';
import IMPORT_MODULE from '../import/import-module';
import PREPARATION_BREADCRUMB_MODULE from '../preparation/breadcrumb/preparation-breadcrumb-module';
import PREPARATION_CREATOR_MODULE from '../preparation/creator/preparation-creator-module';
import PREPARATION_COPY_MOVE_MODULE from '../preparation/copy-move/preparation-copy-move-module';
import PREPARATION_HEADER_MODULE from '../preparation/header/preparation-header-module';
import PREPARATION_LIST from '../preparation/list/preparation-list-module';
import TALEND_WIDGET_MODULE from '../widgets/widget-module';
import SERVICES_DATASET_MODULE from '../../services/dataset/dataset-module';
import SERVICES_DATASET_WORKFLOW_MODULE from '../../services/dataset-workflow/dataset-workflow-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';
import HomeComponent from './home-component';
import HomeDatasetComponent from './dataset/home-dataset-component';
import HomePreparationComponent from './preparation/home-preparation-component';

// React home page
import WIDGETS_CONTAINERS_MODULE from '../widgets-containers/widgets-containers-module';
import ReactHomeComponent from './react-home-component';
import ReactHomePreparationContainer from './preparation/react-home-preparation-container';

const MODULE_NAME = 'data-prep.home';

export default MODULE_NAME;

/**
 * @ngdoc object
 * @name data-prep.home
 * @description This module contains the home page of the app.
 * @requires talend.widget
 * @requires data-prep.dataset-upload-list
 * @requires data-prep.dataset-header
 * @requires data-prep.dataset-list
 * @requires data-prep.import
 * @requires data-prep.preparation-creator
 * @requires data-prep.preparation-copy-move
 * @requires data-prep.preparation-header
 * @requires data-prep.preparation-list
 * @requires data-prep.services.dataset
 * @requires data-prep.services.utils
 * @requires data-prep.services.datasetWorkflowService
 * @requires data-prep.services.state
 * @requires data-prep.services.folder
 */
angular.module(MODULE_NAME,
	[
		DATASET_UPDLOAD_LIST_MODULE,
		DATASET_HEADER_MODULE,
		DATASET_LIST_MODULE,
		IMPORT_MODULE,
		PREPARATION_BREADCRUMB_MODULE,
		PREPARATION_CREATOR_MODULE,
		PREPARATION_COPY_MOVE_MODULE,
		PREPARATION_HEADER_MODULE,
		PREPARATION_LIST,
		TALEND_WIDGET_MODULE,
		WIDGETS_CONTAINERS_MODULE, // react-talend-components containers

		SERVICES_DATASET_MODULE,
		SERVICES_DATASET_WORKFLOW_MODULE,
		SERVICES_STATE_MODULE,
	])
	.component('home', HomeComponent)
	.component('homeDataset', HomeDatasetComponent)
	.component('homePreparation', HomePreparationComponent)
	.component('reactHome', ReactHomeComponent)
	.component('reactHomePreparation', ReactHomePreparationContainer);
