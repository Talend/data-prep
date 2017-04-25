/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';

import actionsHeader from './actions-header-component';

const MODULE_NAME = 'data-prep.actions-header';


/**
 * @ngdoc object
 * @name data-prep.actions-header
 * @description This module display a actions header
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		SERVICES_STATE_MODULE,
	])
	.component('actionsHeader', actionsHeader);

export default MODULE_NAME;
