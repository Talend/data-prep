/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import datasetTour from './onboarding-datasets-constants';
import playgroundTour from './onboarding-playground-constants';
import recipeTour from './onboarding-recipe-constants';
import OnboardingService from './onboarding-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.onboarding
     * @description This module contains the services to manage onboarding tours
     */
    angular.module('data-prep.services.onboarding', [])
        .constant('datasetTour', datasetTour)
        .constant('playgroundTour', playgroundTour)
        .constant('recipeTour', recipeTour)
        .service('OnboardingService', OnboardingService);
})();