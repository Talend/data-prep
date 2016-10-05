/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import TALEND_WIDGET_MODULE from '../widgets/widget-module';
import SERVICES_DATASET_MODULE from '../../services/dataset/dataset-module';
import SERVICES_DATASET_WORKFLOW_MODULE from '../../services/dataset-workflow/dataset-workflow-module';
import SERVICES_IMPORT_MODULE from '../../services/import/import-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';

import Import from './import-component';

const MODULE_NAME = 'data-prep.import';

/**
 * @ngdoc object
 * @name data-prep.import
 * @description import component.
 * @requires talend.widget
 * @requires data-prep.services.dataset
 * @requires data-prep.services.datasetWorkflowService
 * @requires data-prep.services.state
 * @requires data-prep.services.import
 */
angular.module(MODULE_NAME,
	[
		TALEND_WIDGET_MODULE,
		SERVICES_DATASET_MODULE,
		SERVICES_DATASET_WORKFLOW_MODULE,
		SERVICES_IMPORT_MODULE,
		SERVICES_STATE_MODULE,
	])
    .component('import', Import);

export default MODULE_NAME;
