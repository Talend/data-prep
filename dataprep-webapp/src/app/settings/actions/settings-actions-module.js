/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_ONBOARDING_MODULE from '../../services/onboarding/onboarding-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';

import ExternalActionsService from './external-actions-service';
import MenuActionsService from './menu-actions-service';
import ModalActionsService from './modal-actions-service';
import OnboardingActionsService from './onboarding-actions-service';

const MODULE_NAME = 'app.settings.actions';

angular.module(MODULE_NAME,
	[
		SERVICES_ONBOARDING_MODULE,
		SERVICES_STATE_MODULE,
	])
	.service('ExternalActionsService', ExternalActionsService)
	.service('MenuActionsService', MenuActionsService)
	.service('ModalActionsService', ModalActionsService)
	.service('OnboardingActionsService', OnboardingActionsService)
	.factory('SettingsActionsHandlers', function (
		ExternalActionsService,
		MenuActionsService,
		ModalActionsService,
		OnboardingActionsService) {
		'ngInject';
		return [
			ExternalActionsService,
			MenuActionsService,
			ModalActionsService,
			OnboardingActionsService,
		];
	});

export default MODULE_NAME;
