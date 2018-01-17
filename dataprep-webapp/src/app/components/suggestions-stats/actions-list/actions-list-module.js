/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import sunchoke from 'sunchoke';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_EARLY_PREVIEW_MODULE from '../../../services/early-preview/early-preview-module';
import SERVICES_PLAYGROUND_MODULE from '../../../services/playground/playground-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';

import ActionsListCtrl from './actions-list-controller';
import ActionsList from './actions-list-directive';

const MODULE_NAME = 'data-prep.actions-list';

/**
 * @ngdoc object
 * @name data-prep.actions-list
 * @description This module display a transformation actions list
 * @requires talend.widget
 * @requires data-prep.services.playground
 * @requires data-prep.services.realy-preview
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		sunchoke.all,
		TALEND_WIDGET_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_EARLY_PREVIEW_MODULE,
		SERVICES_STATE_MODULE,
	])
    .controller('ActionsListCtrl', ActionsListCtrl)
    .directive('actionsList', ActionsList);

export default MODULE_NAME;
