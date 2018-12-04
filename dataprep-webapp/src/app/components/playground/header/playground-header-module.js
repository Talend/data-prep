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

import PlaygroundHeaderCtrl from './playground-header-controller';
import PlaygroundHeaderComponent from './playground-header-component';

import SERVICES_PLAYGROUND_MODULE from '../../../services/playground/playground-module';


const MODULE_NAME = 'data-prep.playground-header';

/**
 * @ngdoc object
 * @name data-prep.playground-header
 * @description This module contains the component to display the playground header
 */
angular
	.module(MODULE_NAME,
	[
		ngSanitize,
		SERVICES_PLAYGROUND_MODULE,
	])
	.controller('PlaygroundHeaderCtrl', PlaygroundHeaderCtrl)
	.component('playgroundHeader', PlaygroundHeaderComponent);

export default MODULE_NAME;
