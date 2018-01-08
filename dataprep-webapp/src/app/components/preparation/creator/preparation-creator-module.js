/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import DATASET_UPDLOAD_TILE_MODULE from '../../dataset/upload-status/dataset-upload-status-module';
import INVENTORY_ITEM_MODULE from '../../inventory/item/inventory-item-module';
import SERVICES_DATASET_MODULE from '../../../services/dataset/dataset-module';
import SERVICES_DATASET_WORKFLOW_MODULE from '../../../services/dataset-workflow/dataset-workflow-module';
import SERVICES_PREPARATION_MODULE from '../../../services/preparation/preparation-module';
import SERVICES_IMPORT_MODULE from '../../../services/import/import-module';
import SERVICES_UTILS_MODULE from '../../../services/utils/utils-module';

import ThemeFiltersComponent from './theme-filters/theme-filters-component';
import PreparationCreatorFormComponent from './form/preparation-creator-form-component';
import PreparationCreatorContainer from './preparation-creator-container';

const MODULE_NAME = 'data-prep.preparation-creator';

/**
 * @ngdoc object
 * @name data-prep.preparation-creator
 * @description This module creates directly a preparation from a dataset
 */
angular.module(MODULE_NAME,
	[
		DATASET_UPDLOAD_TILE_MODULE,
		INVENTORY_ITEM_MODULE,
		SERVICES_DATASET_MODULE,
		SERVICES_DATASET_WORKFLOW_MODULE,
		SERVICES_PREPARATION_MODULE,
		SERVICES_UTILS_MODULE,
		SERVICES_IMPORT_MODULE,
	])
	.component('themeFilters', ThemeFiltersComponent)
    .component('preparationCreatorForm', PreparationCreatorFormComponent)
    .component('preparationCreator', PreparationCreatorContainer);

export default MODULE_NAME;
