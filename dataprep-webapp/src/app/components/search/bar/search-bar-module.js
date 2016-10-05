/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';

import SERVICES_DATASET_WORKFLOW_MODULE from '../../../services/dataset-workflow/dataset-workflow-module';
import SERVICES_PREPARATION_MODULE from '../../../services/preparation/preparation-module';

import SearchBarComponent from './search-bar-component';

const MODULE_NAME = 'data-prep.search-bar';

/**
 * @ngdoc object
 * @name data-prep.data-prep.search-bar
 * @description This module contains the component to manage search
 * @requires talend.widget
 */
angular.module(MODULE_NAME,
	[
		TALEND_WIDGET_MODULE,
		SERVICES_DATASET_WORKFLOW_MODULE,
		SERVICES_PREPARATION_MODULE,
	])
    .component('searchBar', SearchBarComponent);

export default MODULE_NAME;
