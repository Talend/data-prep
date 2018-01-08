/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

import CanBeBlankValidation from './can-be-blank-validation-directive';
import IsDateTimeValidation from './is-datetime-validation-directive';
import IsTypeValidation from './is-type-validation-directive';
import UniqueFolderValidation from './unique-folder-validation-directive';

const MODULE_NAME = 'data-prep.validation';

/**
 * @ngdoc object
 * @name data-prep.validation
 * @description This module contains the directive to perform type validation on forms
 */
angular.module(MODULE_NAME, [])
    .directive('canBeBlank', CanBeBlankValidation)
    .directive('isDateTime', IsDateTimeValidation)
    .directive('isType', IsTypeValidation)
    .directive('uniqueFolder', UniqueFolderValidation);

export default MODULE_NAME;
