/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import BREADCRUMB_MODULE from '../../breadcrumb/breadcrumb-module';
import SERVICES_FOLDER_MODULE from '../../../services/folder/folder-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';

import PreparationBreadcrumbComponent from './preparation-breadcrumb-component';

const MODULE_NAME = 'data-prep.preparation-breadcrumb';

angular.module(MODULE_NAME,
	[
		BREADCRUMB_MODULE,
		SERVICES_FOLDER_MODULE,
		SERVICES_STATE_MODULE,
	])
    .component('preparationBreadcrumb', PreparationBreadcrumbComponent);

export default MODULE_NAME;
