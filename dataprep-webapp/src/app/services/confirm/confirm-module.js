/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_STATE_MODULE from '../state/state-module';

import ConfirmService from './confirm-service';

const MODULE_NAME = 'data-prep.services.confirm';

/**
 * @ngdoc object
 * @name data-prep.services.confirm
 * @description This module contains the services to manage the confirmation modals
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		SERVICES_STATE_MODULE,
	])
    .service('ConfirmService', ConfirmService);

export default MODULE_NAME;
