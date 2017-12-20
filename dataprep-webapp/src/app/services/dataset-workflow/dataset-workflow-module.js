/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import uiRouter from 'angular-ui-router';
import SERVICES_DATASET_MODULE from '../dataset/dataset-module';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_MESSAGE_MODULE from '../message/message-module';

import UploadWorkflowService from './upload-workflow-service';
import UpdateWorkflowService from './update-workflow-service';

const MODULE_NAME = 'data-prep.services.datasetWorkflowService';

/**
 * @ngdoc object
 * @name data-prep.services.dataset-workflow
 * @description This module contains the services to manage the upload & update of datasets
 * @requires ui.router
 * @requires data-prep.services.dataset
 * @requires data-prep.services.state
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		uiRouter,
		SERVICES_DATASET_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_MESSAGE_MODULE,
	])
    .service('UploadWorkflowService', UploadWorkflowService)
    .service('UpdateWorkflowService', UpdateWorkflowService);

export default MODULE_NAME;
