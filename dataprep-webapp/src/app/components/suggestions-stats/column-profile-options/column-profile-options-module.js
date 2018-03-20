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
import TALEND_WIDGET from '../../widgets/widget-module';

import ColumnProfileOptionsComponent from './column-profile-options-component';

const MODULE_NAME = 'data-prep.column-profile-options';

/**
 * @ngdoc object
 * @name data-prep.column-profile-options
 * @description Column profile (charts) options
 */
angular.module(MODULE_NAME, [ngTranslate, TALEND_WIDGET])
    .component('columnProfileOptions', ColumnProfileOptionsComponent);

export default MODULE_NAME;
