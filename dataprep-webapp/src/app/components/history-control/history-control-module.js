/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_HISTORY_MODULE from '../../services/history/history-module';

import HistoryControl from './history-control-directive';

const MODULE_NAME = 'data-prep.history-control';

/**
 * @ngdoc object
 * @name data-prep.history-control
 * @description This module contains the history control buttons and shortcut
 * @requires data-prep.services.history
 */
angular.module(MODULE_NAME, [SERVICES_HISTORY_MODULE])
    .directive('historyControl', HistoryControl);

export default MODULE_NAME;
