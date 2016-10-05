/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import uiRouter from 'angular-ui-router';
import INVENTORY_SEARCH_MODULE from '../search/inventory/inventory-search-module';
import SERVICES_DATASET_MODULE from '../../services/dataset/dataset-module';
import SERVICES_FEEDBACK_MODULE from '../../services/feedback/feedback-module';
import SERVICES_ONBOARDING_MODULE from '../../services/onboarding/onboarding-module';
import SERVICES_UTILS_MODULE from '../../services/utils/utils-module';

import NavbarCtrl from './navbar-controller';
import Navbar from './navbar-directive';

const MODULE_NAME = 'data-prep.navbar';

angular.module(MODULE_NAME,
	[
		uiRouter,
		INVENTORY_SEARCH_MODULE,
		SERVICES_DATASET_MODULE,
		SERVICES_FEEDBACK_MODULE,
		SERVICES_ONBOARDING_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .controller('NavbarCtrl', NavbarCtrl)
    .directive('navbar', Navbar);

export default MODULE_NAME;
