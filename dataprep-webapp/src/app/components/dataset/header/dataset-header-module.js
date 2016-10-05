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
import INVENTORY_HEADER_MODULE from '../../inventory/header/inventory-header-module';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_DATASET_MODULE from '../../../services/dataset/dataset-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';

import DatasetHeader from './dataset-header-component';

const MODULE_NAME = 'data-prep.dataset-header';

/**
 * @ngdoc object
 * @name data-prep.dataset-header
 * @description This module contains the entities to manage the dataset list header
 * @requires talend.widget
 * @requires data-prep.inventory-header
 * @requires data-prep.services.dataset
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		ngTranslate,
		INVENTORY_HEADER_MODULE,
		TALEND_WIDGET_MODULE,
		SERVICES_DATASET_MODULE,
		SERVICES_STATE_MODULE,
	])
    .component('datasetHeader', DatasetHeader);

export default MODULE_NAME;
