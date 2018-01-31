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
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';
import SERVICES_TRANSFORMATION_MODULE from '../../../services/transformation/transformation-module';

import TabItemCtrl from './tab-item-controller';
import TabItem from './tab-item-directive';

const MODULE_NAME = 'data-prep.tab-item';

/**
 * @ngdoc object
 * @name data-prep.tab-item
 * @description This module contains the entities to manage tab item
 * @requires talend.widget
 * @requires data-prep.services.transformation
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		uiRouter,
		TALEND_WIDGET_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_TRANSFORMATION_MODULE,
	])
    .controller('TabItemCtrl', TabItemCtrl)
    .directive('tabItem', TabItem);

export default MODULE_NAME;
