/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_PREVIEW_MODULE from '../preview/preview-module';
import SERVICES_RECIPE_MODULE from '../recipe/recipe-module';
import SERVICES_STATE_MODULE from '../state/state-module';

import EarlyPreviewService from './early-preview-service';

const MODULE_NAME = 'data-prep.services.early-preview';

/**
 * @ngdoc object
 * @name data-prep.services.early-preview
 * @description This module contains the early preview services
 * @requires data-prep.services.preview
 * @requires data-prep.services.recipe
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		SERVICES_PREVIEW_MODULE,
		SERVICES_RECIPE_MODULE,
		SERVICES_STATE_MODULE,
	])
    .service('EarlyPreviewService', EarlyPreviewService);

export default MODULE_NAME;
