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
import uiRouter from 'angular-ui-router';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_DATASET_MODULE from '../../../services/dataset/dataset-module';
import SERVICES_PLAYGROUND_MODULE from '../../../services/playground/playground-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';
import SERVICES_UTILS_MODULE from '../../../services/utils/utils-module';

import DatasetXlsPreviewCtrl from './dataset-xls-preview-controller';
import DatasetXlsPreview from './dataset-xls-preview-directive';

const MODULE_NAME = 'data-prep.dataset-xls-preview';

/**
 * @ngdoc object
 * @name data-prep.dataset-xls-preview
 * @description This module contains the entities to manage the dataset xls preview
 * @requires talend.widget
 * @requires data-prep.services.dataset
 * @requires data-prep.services.playground
 * @requires data-prep.services.utils
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		ngTranslate,
		uiRouter,
		TALEND_WIDGET_MODULE,
		SERVICES_DATASET_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .controller('DatasetXlsPreviewCtrl', DatasetXlsPreviewCtrl)
    .directive('datasetXlsPreview', DatasetXlsPreview);

export default MODULE_NAME;
