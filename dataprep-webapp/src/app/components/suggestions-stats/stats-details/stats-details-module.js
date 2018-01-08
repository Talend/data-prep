/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_FILTER_MANAGER_MODULE from '../../../services/filter/manager/filter-manager-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';

import StatsDetailsCtrl from './stats-details-controller';
import StatsDetails from './stats-details-directive';

const MODULE_NAME = 'data-prep.stats-details';

/**
 * @ngdoc object
 * @name data-prep.stats-details
 * @description This module contains the controller and directives for the statistics tabs
 * @requires talend.widget
 * @requires data-prep.services.state
 * @requires data-prep.services.filter
 */
angular.module(MODULE_NAME,
	[
		TALEND_WIDGET_MODULE,
		SERVICES_FILTER_MANAGER_MODULE,
		SERVICES_STATE_MODULE,
	])
    .controller('StatsDetailsCtrl', StatsDetailsCtrl)
    .directive('statsDetails', StatsDetails);

export default MODULE_NAME;
