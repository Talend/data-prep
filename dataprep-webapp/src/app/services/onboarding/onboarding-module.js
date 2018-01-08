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
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_UTILS_MODULE from './../utils/utils-module';

import playgroundTour from './onboarding-playground-constants';
import recipeTour from './onboarding-recipe-constants';
import preparationTour from './onboarding-preparations-constants';
import OnboardingService from './onboarding-service';


const MODULE_NAME = 'data-prep.services.onboarding';

/**
 * @ngdoc object
 * @name data-prep.services.onboarding
 * @description This module contains the services to manage onboarding tours
 */
angular.module(MODULE_NAME,
	[
		uiRouter,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .constant('playgroundTour', playgroundTour)
    .constant('recipeTour', recipeTour)
    .constant('preparationTour', preparationTour)
    .service('OnboardingService', OnboardingService);

export default MODULE_NAME;
