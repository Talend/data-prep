/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import uiRouter from 'angular-ui-router';
import SERVICES_DATASET_WORKFLOW_MODULE from '../../services/dataset-workflow/dataset-workflow-module';
import SERVICES_FOLDER_MODULE from '../../services/folder/folder-module';
import SERVICES_IMPORT_MODULE from '../../services/import/import-module';
import SERVICES_ONBOARDING_MODULE from '../../services/onboarding/onboarding-module';
import SERVICES_PREPARATION_MODULE from '../../services/preparation/preparation-module';
import SERVICES_SEARCH_MODULE from '../../services/search/search-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';
import SERVICES_UTILS_MODULE from '../../services/utils/utils-module';
import WIDGETS_MODULE from '../../components/widgets/widget-module';

import ExternalActionsService from './external-actions-service';
import MenuActionsService from './menu-actions-service';
import ModalActionsService from './modal-actions-service';
import OnboardingActionsService from './onboarding-actions-service';
import DatasetActionsService from './dataset-actions-service';
import PreparationActionsService from './preparation-actions-service';
import InventoryActionsService from './inventory-actions-service';
import SidePanelActionsService from './sidepanel-actions-service';
import SearchActionsService from './search-actions-service';

const MODULE_NAME = 'app.settings.actions';

angular.module(MODULE_NAME,
	[
		uiRouter,
		SERVICES_DATASET_WORKFLOW_MODULE,
		SERVICES_FOLDER_MODULE,
		SERVICES_IMPORT_MODULE,
		SERVICES_ONBOARDING_MODULE,
		SERVICES_PREPARATION_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_SEARCH_MODULE,
		SERVICES_UTILS_MODULE,
		WIDGETS_MODULE,
	])
	.service('ExternalActionsService', ExternalActionsService)
	.service('MenuActionsService', MenuActionsService)
	.service('ModalActionsService', ModalActionsService)
	.service('OnboardingActionsService', OnboardingActionsService)
	.service('DatasetActionsService', DatasetActionsService)
	.service('PreparationActionsService', PreparationActionsService)
	.service('InventoryActionsService', InventoryActionsService)
	.service('SidePanelActionsService', SidePanelActionsService)
	.service('SearchActionsService', SearchActionsService)
	.factory('SettingsActionsHandlers', function (
		ExternalActionsService,
		MenuActionsService,
		ModalActionsService,
		OnboardingActionsService,
		DatasetActionsService,
		PreparationActionsService,
		InventoryActionsService,
		SearchActionsService,
		SidePanelActionsService) {
		'ngInject';
		return [
			ExternalActionsService,
			MenuActionsService,
			ModalActionsService,
			OnboardingActionsService,
			DatasetActionsService,
			PreparationActionsService,
			SearchActionsService,
			InventoryActionsService,
			SidePanelActionsService,
		];
	});

export default MODULE_NAME;
