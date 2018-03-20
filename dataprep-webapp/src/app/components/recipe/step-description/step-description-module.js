/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngSanitize from 'angular-sanitize';
import ngTranslate from 'angular-translate';

import StepDescription from './step-description-component';

const MODULE_NAME = 'data-prep.step-description';

/**
 * @ngdoc object
 * @name data-prep.step-description
 * @description This module creates the recipe step details
 */
angular.module(MODULE_NAME,
	[
		ngSanitize,
		ngTranslate,
	])
    .component('stepDescription', StepDescription);

export default MODULE_NAME;
