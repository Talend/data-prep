/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_STATE_MODULE from '../state/state-module';

import MessageService from './message-service';

const MODULE_NAME = 'data-prep.services.message';

/**
 * @ngdoc object
 * @name data-prep.services.message
 * @description This module contains the message service
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME, [SERVICES_STATE_MODULE])
    .service('MessageService', MessageService);

export default MODULE_NAME;
