/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import 'ng-file-upload/dist/angular-file-upload-all';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import DatasetSheetPreviewService from './preview/dataset-sheet-preview-service';
import DatasetListService from './list/dataset-list-service';
import DatasetRestService from './rest/dataset-rest-service';
import DatasetService from './dataset-service';

const MODULE_NAME = 'data-prep.services.dataset';

/**
 * @ngdoc object
 * @name data-prep.services.dataset
 * @description This module contains the services to manipulate datasets
 * @requires data-prep.services.folder
 * @requires data-prep.services.state
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		'angularFileUpload', // file upload with progress support
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('DatasetSheetPreviewService', DatasetSheetPreviewService)
    .service('DatasetRestService', DatasetRestService)
    .service('DatasetListService', DatasetListService)
    .service('DatasetService', DatasetService);

export default MODULE_NAME;
