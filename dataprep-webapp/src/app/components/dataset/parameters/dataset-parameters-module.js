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
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import TALEND_WIDGET_CONTAINERS_MODULE from '../../widgets-containers/widgets-containers-module';
import SERVICES_DATASET_MODULE from '../../../services/dataset/dataset-module';
import SERVICES_PLAYGROUND_MODULE from '../../../services/playground/playground-module';

import DatasetParametersCtrl from './dataset-parameters-controller';
import DatasetParameters from './dataset-parameters-directive';

const MODULE_NAME = 'data-prep.dataset-parameters';

/**
 * @ngdoc object
 * @name data-prep.dataset-parameters
 * @description This module contains the entities to manage the dataset parameters
 * @requires talend.widget
 * @requires data-prep.services.dataset
 * @requires data-prep.services.playground
 */
angular.module(MODULE_NAME,
	[
		ngTranslate,
		TALEND_WIDGET_MODULE,
		SERVICES_DATASET_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		TALEND_WIDGET_CONTAINERS_MODULE,
	])
    .controller('DatasetParametersCtrl', DatasetParametersCtrl)
    .directive('datasetParameters', DatasetParameters);

export default MODULE_NAME;
