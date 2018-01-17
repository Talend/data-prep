/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import TRANSFORMATION_FORM_MODULE from '../../transformation/form/transformation-form-module';
import TYPE_TRANSFORMATION_MENU from '../../transformation/menu/type/type-transformation-menu-module';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_PARAMETERS_MODULE from '../../../services/parameters/parameters-module';
import SERVICES_PLAYGROUND_MODULE from '../../../services/playground/playground-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';
import SERVICES_TRANSFORMATION_MODULE from '../../../services/transformation/transformation-module';

import TransformMenuCtrl from './transformation-menu-controller';
import TransformMenu from './transformation-menu-directive';

const MODULE_NAME = 'data-prep.transformation-menu';

/**
 * @ngdoc object
 * @name data-prep.transformation-menu
 * @description This module contains the controller
 * and directives to manage the transformation menu items
 * @requires talend.widget
 * @requires data-prep.transformation-form
 * @requires data-prep.type-transformation-menu
 * @requires data-prep.services.parameters
 * @requires data-prep.services.playground
 * @requires data-prep.services.transformation
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		TALEND_WIDGET_MODULE,
		TRANSFORMATION_FORM_MODULE,
		TYPE_TRANSFORMATION_MENU,
		SERVICES_PARAMETERS_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_TRANSFORMATION_MODULE,
	])
    .controller('TransformMenuCtrl', TransformMenuCtrl)
    .directive('transformMenu', TransformMenu);

export default MODULE_NAME;
