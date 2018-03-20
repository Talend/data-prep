/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngTranslate from 'angular-translate';
import DatasetUploadStatus from './dataset-upload-status-directive';

const MODULE_NAME = 'data-prep.dataset-upload-status';

/**
 * @ngdoc object
 * @name data-prep.dataset-upload-status
 * @description This module contains the entities to manage the dataset upload tile
 */
angular.module(MODULE_NAME, [ngTranslate])
	.directive('datasetUploadStatus', DatasetUploadStatus);

export default MODULE_NAME;
