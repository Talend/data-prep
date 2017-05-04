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
import DatasetUploadTile from './dataset-upload-tile-directive';

const MODULE_NAME = 'data-prep.dataset-upload-tile';

/**
 * @ngdoc object
 * @name data-prep.dataset-upload-tile
 * @description This module contains the entities to manage the dataset upload tile
 */
angular.module(MODULE_NAME, [ngTranslate])
	.directive('datasetUploadTile', DatasetUploadTile);

export default MODULE_NAME;
