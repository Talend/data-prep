/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

import HistoryService from './history-service';

const MODULE_NAME = 'data-prep.services.history';

/**
 * @ngdoc object
 * @name data-prep.services.history
 * @description This module contains the services to manage actions history
 */
angular.module(MODULE_NAME, [])
    .service('HistoryService', HistoryService);

export default MODULE_NAME;
