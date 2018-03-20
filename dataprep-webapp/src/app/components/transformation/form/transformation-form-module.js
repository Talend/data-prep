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
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import VALIDATION_MODULE from '../../validation/validation-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';
import SERVICES_TRANSFORMATION_UTILS_MODULE from '../../../services/transformation/transformation-module';
import SERVICES_UTILS_MODULE from '../../../services/utils/utils-module';

import TransformChoiceParamCtrl from './choice/transformation-choice-param-controller';
import TransformChoiceParam from './choice/transformation-choice-param-directive';
import TransformClusterParamsCtrl from './cluster/transformation-cluster-params-controller';
import TransformClusterParams from './cluster/transformation-cluster-params-directive';
import TransformColumnParamCtrl from './column/transformation-column-param-controller';
import TransformColumnParam from './column/transformation-column-param-directive';
import TransformDateParamCtrl from './date/transformation-date-param-controller';
import TransformDateParam from './date/transformation-date-param-directive';
import TransformRegexParamCtrl from './regex/transformation-regex-param-controller';
import TransformRegexParam from './regex/transformation-regex-param-directive';
import TransformSimpleParamCtrl from './simple/transformation-simple-param-controller';
import TransformSimpleParam from './simple/transformation-simple-param-directive';
import TransformParamsCtrl from './params/transformation-params-controller';
import TransformParams from './params/transformation-params-directive';
import TransformForm from './transformation-form-directive';
import TransformFormCtrl from './transformation-form-controller';

const MODULE_NAME = 'data-prep.transformation-form';

/**
 * @ngdoc object
 * @name data-prep.transformation-form
 * @description This module contains the controller
 * and directives to manage transformation parameters
 * @requires data-prep.services.state
 * @requires data-prep.services.utils.service
 * @requires data-prep.validation
 */
angular.module(MODULE_NAME,
	[
		ngSanitize,
		TALEND_WIDGET_MODULE,
		VALIDATION_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_TRANSFORMATION_UTILS_MODULE,
		SERVICES_UTILS_MODULE,
	])

    .controller('TransformChoiceParamCtrl', TransformChoiceParamCtrl)
    .directive('transformChoiceParam', TransformChoiceParam)

    .controller('TransformClusterParamsCtrl', TransformClusterParamsCtrl)
    .directive('transformClusterParams', TransformClusterParams)

    .controller('TransformColumnParamCtrl', TransformColumnParamCtrl)
    .directive('transformColumnParam', TransformColumnParam)

    .controller('TransformDateParamCtrl', TransformDateParamCtrl)
    .directive('transformDateParam', TransformDateParam)

    .controller('TransformRegexParamCtrl', TransformRegexParamCtrl)
    .directive('transformRegexParam', TransformRegexParam)

    .controller('TransformSimpleParamCtrl', TransformSimpleParamCtrl)
    .directive('transformSimpleParam', TransformSimpleParam)

    .controller('TransformParamsCtrl', TransformParamsCtrl)
    .directive('transformParams', TransformParams)

    .controller('TransformFormCtrl', TransformFormCtrl)
    .directive('transformForm', TransformForm);

export default MODULE_NAME;
