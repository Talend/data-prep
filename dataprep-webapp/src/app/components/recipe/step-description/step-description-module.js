/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import StepDescription from './step-description-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.step-description
     * @description This module creates the recipe step details
     */
    angular.module('data-prep.step-description', [
            'pascalprecht.translate',
            'ngSanitize',
        ])
        .component('stepDescription', StepDescription);
})();