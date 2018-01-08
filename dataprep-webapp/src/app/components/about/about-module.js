/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngTranslate from 'angular-translate';

import AboutComponent from './about-component.js';
import SERVICES_ABOUT_MODULE from '../../services/about/about-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';
import SERVICES_UTILS_MODULE from '../../services/utils/utils-module';

const MODULE_NAME = 'data-prep.about';

angular.module(MODULE_NAME, [
	ngTranslate,
	SERVICES_ABOUT_MODULE,
	SERVICES_STATE_MODULE,
	SERVICES_UTILS_MODULE,
])
	.component('about', AboutComponent);

export default MODULE_NAME;
